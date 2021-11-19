package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.TEMPORARY_REDIRECT;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.lang3.StringUtils.isAllBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.skvf.kaninregister.model.Bunny.WILDCARD;
import static se.skvf.kaninregister.model.Bunny.byBreeder;
import static se.skvf.kaninregister.model.Bunny.byExactIdentifier;
import static se.skvf.kaninregister.model.Bunny.byOwner;
import static se.skvf.kaninregister.model.Bunny.byPreviousOwner;
import static se.skvf.kaninregister.model.Bunny.byWildcardIdentifier;
import static se.skvf.kaninregister.model.Bunny.splitIdentifiers;
import static se.skvf.kaninregister.model.Bunny.IdentifierLocation.CHIP;
import static se.skvf.kaninregister.model.Bunny.IdentifierLocation.RING;
import static se.skvf.kaninregister.model.Owner.byUserName;
import static se.skvf.kaninregister.model.Owner.newOwner;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import se.skvf.kaninregister.addo.AddoSigningService;
import se.skvf.kaninregister.addo.Signature;
import se.skvf.kaninregister.addo.Signing;
import se.skvf.kaninregister.api.BunnyDTO.GenderEnum;
import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Bunny.Gender;
import se.skvf.kaninregister.model.Bunny.IdentifierLocation;
import se.skvf.kaninregister.model.Owner;
import se.skvf.kaninregister.model.Registry;

@Provider
public class BunnyRegistryApiImpl implements BunnyRegistryApi {

	private static final Log LOG = LogFactory.getLog(BunnyRegistryApiImpl.class);

	static final String SESSION_SIGNING = "signing";
	
	@Autowired
	private Registry registry;
	@Autowired
	private SessionManager sessions;
	@Autowired
	private AddoSigningService signingService;
	
	@Value("${skvf.approval.url:}")
	private String approvalUrl;
	
	void setApprovalUrl(String approvalUrl) {
		this.approvalUrl = approvalUrl;
	}
	
	@Context
	private HttpServletRequest request;
	@Context
	private HttpServletResponse response;
	
	@Override
	public BunnyDTO createBunny(String ownerId, BunnyDTO bunnyDTO) {
		return process(() -> {
			
			validateSession(ownerId);
			if (bunnyDTO.getId() != null || 
					(bunnyDTO.getOwner() != null && !bunnyDTO.getOwner().equals(ownerId))) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			validateOwner(ownerId, true);
			
			bunnyDTO.setOwner(ownerId);
			bunnyDTO.setId(registry.add(toBunny(bunnyDTO)));
			return bunnyDTO;
		});
	}

	private static <T> T process(Callable<T> call) {
		try {
			return call.call();
		} catch (WebApplicationException e) {
			LOG.info("Application error", e);
			throw e;
		} catch (IllegalStateException e) {
			LOG.info("Application error", e);
			throw new WebApplicationException(e, BAD_REQUEST);	
		} catch (Exception e) {
			LOG.error("Unexpected error", e);
			throw new WebApplicationException(e, INTERNAL_SERVER_ERROR);
		}
	}
	
	private Bunny toBunny(BunnyDTO dto) throws IOException {
		
		checkUniqueIdentifier(CHIP, dto.getChip());
		checkUniqueIdentifier(RING, dto.getRing());
		
		return new Bunny().setId(dto.getId())
				.setName(dto.getName())
				.setOwner(dto.getOwner())
				.setBreeder(dto.getBreeder())
				.setBirthDate(dto.getBirthDate())
				.setChip(dto.getChip())
				.setCoat(dto.getCoat())
				.setColourMarkings(dto.getColourMarkings())
				.setFeatures(dto.getFeatures())
				.setGender(toGender(dto.getGender()))
				.setLeftEar(dto.getLeftEar())
				.setNeutered(ofNullable(dto.getNeutered()).orElse(false))
				.setPicture(dto.getPicture())
				.setRace(dto.getRace())
				.setRightEar(dto.getRightEar())
				.setRing(dto.getRing());
	}
	
	private static BunnyDTO toDTO(Bunny bunny) {
		BunnyDTO dto = new BunnyDTO();
		dto.setId(bunny.getId());
		dto.setName(bunny.getName());
		dto.setOwner(bunny.getOwner());
		dto.setPreviousOwner(bunny.getPreviousOwner());
		dto.setBreeder(bunny.getBreeder());
		dto.setBirthDate(bunny.getBirthDate());
		dto.setChip(bunny.getChip());
		dto.setCoat(bunny.getCoat());
		dto.setColourMarkings(bunny.getColourMarkings());
		dto.setFeatures(bunny.getFeatures());
		dto.setGender(toGender(bunny.getGender()));
		dto.setLeftEar(bunny.getLeftEar());
		dto.setNeutered(bunny.isNeutered());
		dto.setPicture(bunny.getPicture());
		dto.setRace(bunny.getRace());
		dto.setRightEar(bunny.getRightEar());
		dto.setRing(bunny.getRing());
		return dto;
	}
	
	private static BunnyListDTO toListDTO(Bunny bunny) {
		BunnyListDTO dto = new BunnyListDTO();
		dto.setId(bunny.getId());
		dto.setName(bunny.getName());
		dto.setChip(bunny.getChip());
		dto.setLeftEar(bunny.getLeftEar());
		dto.setRightEar(bunny.getRightEar());
		dto.setRing(bunny.getRing());
		return dto;
	}
	
	private static OwnerDTO toDTO(Owner owner) {
		OwnerDTO dto = new OwnerDTO();
		dto.setId(owner.getId());
		dto.setName(owner.getName());
		dto.setEmail(owner.getEmail());
		dto.setAddress(owner.getAddress());
		dto.setPhone(owner.getPhone());
		dto.setUserName(owner.getUserName());
		dto.setPublicOwner(owner.isPublicOwner());
		dto.setBreederName(owner.getBreederName());
		dto.setBreederEmail(owner.getBreederEmail());
		dto.setPublicBreeder(owner.isPublicBreeder());
		return dto;
	}
	
	private static BunnyOwnerDTO toOwnerDTO(Owner owner) {
		BunnyOwnerDTO dto = new BunnyOwnerDTO();
		dto.setName(owner.getName());
		dto.setEmail(owner.getEmail());
		dto.setAddress(owner.getAddress());
		dto.setPhone(owner.getPhone());
		return dto;
	}
	
	private static BunnyBreederDTO toBreederDTO(Owner owner) {
		BunnyBreederDTO dto = new BunnyBreederDTO();
		dto.setName(ofNullable(owner.getBreederName())
				.orElse(owner.getName()));
		dto.setEmail(ofNullable(owner.getBreederEmail())
				.orElse(owner.getEmail()));
		return dto;
	}
	
	private String getSession() {
		return ofNullable(getSessionCookie())
				.map(Cookie::getValue)
				.orElse(null);
	}
	
	private Cookie getSessionCookie() {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(BunnyRegistryApi.class.getSimpleName())) {
					return cookie;
				}
			}
		}
		return null;
	}
	
	private void setSession(String sessionId) {
		Cookie cookie = new Cookie(BunnyRegistryApi.class.getSimpleName(), sessionId);
		//cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(-1);
		response.addCookie(cookie);
	}
	
	private String validateSession(String ownerId) {
		String session = getSession();
		if (!sessions.isSession(session, ownerId)) {
			throw new WebApplicationException(UNAUTHORIZED);
		}
		return session;
	}

	@Override
	public BunnyList findBunnies(List<BunnyIdentifierLocation> locations, List<String> identifiers) {
		return process(() -> {

			if (CollectionUtils.isEmpty(locations) || 
					CollectionUtils.isEmpty(identifiers) ||
					locations.size() != identifiers.size()) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			identifiers.forEach(identifier -> {
				long wildcardCount = identifier.chars()
						.filter(c -> c == WILDCARD)
						.count();
				if (wildcardCount > 2) {
					throw new WebApplicationException(BAD_REQUEST);
				}
			});
			
			Map<String, Predicate<String>> filter = new HashMap<>();
			for (int i=0;i<locations.size();i++) {
				filter.putAll(byWildcardIdentifier(location(locations.get(i)), identifiers.get(i)));
			}
			Collection<Bunny> bunnies = registry.findBunnies(filter);
			
			if (bunnies.size() > 10) {
				throw new WebApplicationException(NO_CONTENT);
			}
			
			BunnyList list = new BunnyList();
			bunnies.stream()
				.map(BunnyRegistryApiImpl::toListDTO)
				.forEach(list.getBunnies()::add);
			return list;
		});
	}

	@Override
	public BunnyDTO getBunny(String id) {
		return process(() -> toDTO(validateBunny(id)));
	}

	@Override
	public BunnyBreederDTO getBunnyBreeder(String id) {
		return process(() -> {
			
			Owner breeder = validateOwner(validateBunny(id).getBreeder(), false);
			if (breeder.isNotPublicBreeder()) {
				throw new WebApplicationException(NO_CONTENT);
			}
			return toBreederDTO(breeder);
		});
	}

	private Owner validateOwner(String id, boolean requiresApproval) throws IOException {
		if (isBlank(id)) {
			throw new WebApplicationException(NOT_FOUND);
		}
		Collection<Owner> owners = registry.findOwners(singleton(id));
		if (owners.isEmpty()) {
			throw new WebApplicationException(NOT_FOUND);
		}
		Owner owner = owners.iterator().next();
		if (requiresApproval && !owner.isApproved()) {
			throw new WebApplicationException(UNAUTHORIZED);
		}
		return owner;
	}

	private Bunny validateBunny(String id) throws IOException {
		if (isBlank(id)) {
			throw new WebApplicationException(NOT_FOUND);
		}
		Collection<Bunny> bunnies = registry.findBunnies(singleton(id));
		if (bunnies.isEmpty()) {
			throw new WebApplicationException(NOT_FOUND);
		}
		return bunnies.iterator().next();
	}

	@Override
	public BunnyOwnerDTO getBunnyOwner(String id) {
		return process(() -> {
			
			Owner owner = validateOwner(validateBunny(id).getOwner(), false);
			if (owner.isNotPublicOwner()) {
				throw new WebApplicationException(NO_CONTENT);
			}
			return toOwnerDTO(owner);
		});
	}

	@Override
	public OwnerDTO getOwner(String id) {
		return process(() -> {
			
			String session = getSession();
			if (session == null) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			Owner owner = validateOwner(id, false);
			if (owner.isNotPublicOwner() &&
					!sessions.isSession(session, owner.getId())) {
				throw new WebApplicationException(NO_CONTENT);
			}
			
			return toDTO(owner);
		});
	}

	@Override
	public BunnyList getOwnerBunnies(String id) {
		return process(() -> {
			
			validateSession(id);
			validateOwner(id, false);
			
			BunnyList bunnies = toBunnyList(registry.findBunnies(byOwner(id)));
			addTransferBunnies(bunnies, id);
			return bunnies;
		});
	}

	private void addTransferBunnies(BunnyList bunnies, String id) throws IOException {
		Collection<Bunny> transfers = registry.findBunnies(byPreviousOwner(id));
		if (transfers.size() > 0) {
			Set<String> newOwners = transfers.stream().map(Bunny::getOwner).collect(toSet());
			Map<String, Owner> owners = registry.findOwners(newOwners).stream().collect(toMap(Owner::getId, identity()));
			for (Bunny transfer : transfers) {
				if (!owners.get(transfer.getOwner()).isActivated()) {
					BunnyListDTO dto = toListDTO(transfer);
					dto.setClaimToken(transfer.getOwner());
					bunnies.getBunnies().add(dto);
				}
			}
		}
	}

	private BunnyList toBunnyList(Collection<Bunny> bunnies) {
		BunnyList list = new BunnyList();
		list.setBunnies(bunnies.stream().map(BunnyRegistryApiImpl::toListDTO).collect(Collectors.toList()));
		return list;
	}

	@Override
	public OwnerDTO session() {
		return process(() -> {
			String session = getSession();
			if (session == null) {
				throw new WebApplicationException(UNAUTHORIZED);
			}

			String ownerId = sessions.getOwnerIdForSession(session);
			if (isBlank(ownerId)) {
				throw new WebApplicationException(UNAUTHORIZED);
			}

			return toDTO(validateOwner(ownerId, false));
		});
	};

	@Override
	public OwnerDTO login(LoginDTO loginDTO) {
		return process(() -> {

			if (getSession() != null) {
				throw new WebApplicationException(CONFLICT);
			}
			if (loginDTO.getUserName() == null) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			Collection<Owner> owners = registry.findOwners(byUserName(loginDTO.getUserName()));
			if (owners.isEmpty()) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			Owner owner = owners.iterator().next();
			
			if (!owner.validate(loginDTO.getPassword())) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			setSession(sessions.startSession(owner.getId()));
			return toDTO(owner);
		});
	}

	@Override
	public void logout() {
		sessions.endSession(getSession());
		removeCookie();
	}

	private void removeCookie() {
		ofNullable(getSessionCookie()).ifPresent(c -> {
			c.setMaxAge(0);
			c.setPath("/"); //TODO: Why is path not set on the cookie already?
			response.addCookie(c);
		});
	}

	@Override
	public void setPassword(String ownerId, PasswordDTO passwordDTO) {
		process(() -> {
			
			if (isAllBlank(passwordDTO.getNewPassword())) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			Owner owner = validateOwner(ownerId, false);
			
			if (!owner.validate(passwordDTO.getCurrentPassword())) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			registry.update(owner.setPassword(passwordDTO.getNewPassword()));
			return Void.class;
		});
	}

	@Override
	public BunnyDTO updateBunny(String ownerId, String bunnyId, BunnyDTO bunnyDTO) {
		return process(() -> {
			
			validateSession(ownerId);
			validateOwner(ownerId, true);
			
			if (bunnyDTO.getId() != null &&
					!bunnyDTO.getId().equals(bunnyId)) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			Bunny bunny = validateBunny(bunnyId);
			
			if (bunnyDTO.getBreeder() != null &&
					!bunnyDTO.getBreeder().equals(bunny.getBreeder())) {
				bunnyDTO.setBreeder("");
			}
			
			update(bunny, bunnyDTO);
			registry.update(bunny);
			return toDTO(bunny);
		});
	}

	private void update(Bunny bunny, BunnyDTO dto) throws IOException {
		
		checkUniqueIdentifier(bunny, CHIP, dto.getChip());
		checkUniqueIdentifier(bunny, RING, dto.getRing());
		
		ofNullable(dto.getBreeder()).ifPresent(bunny::setBreeder);
		ofNullable(dto.getName()).ifPresent(bunny::setName);
		ofNullable(dto.getBirthDate()).ifPresent(bunny::setBirthDate);
		ofNullable(dto.getChip()).ifPresent(bunny::setChip);
		ofNullable(dto.getCoat()).ifPresent(bunny::setCoat);
		ofNullable(dto.getColourMarkings()).ifPresent(bunny::setColourMarkings);
		ofNullable(dto.getFeatures()).ifPresent(bunny::setFeatures);
		ofNullable(dto.getGender()).ifPresent(g -> bunny.setGender(toGender(g)));
		ofNullable(dto.getLeftEar()).ifPresent(bunny::setLeftEar);
		ofNullable(dto.getNeutered()).ifPresent(bunny::setNeutered);
		ofNullable(dto.getPicture()).ifPresent(bunny::setPicture);
		ofNullable(dto.getPreviousOwner()).ifPresent(bunny::setPreviousOwner);
		ofNullable(dto.getRace()).ifPresent(bunny::setRace);
		ofNullable(dto.getRightEar()).ifPresent(bunny::setRightEar);
		ofNullable(dto.getRing()).ifPresent(bunny::setRing);
	}

	private void checkUniqueIdentifier(Bunny bunny, IdentifierLocation location, String newIdentifier) throws IOException {
		
		if (isNotEmpty(newIdentifier) && !newIdentifier.equals(bunny.getIdentifier(location))) {
			for (String identifier : splitIdentifiers(newIdentifier)) {
				Collection<Bunny> bunnies = registry.findBunnies(byExactIdentifier(location, identifier));
				if (bunnies.size() > 0 && 
					bunnies.stream().noneMatch(b -> bunny.getId().equals(b.getId()))) {
					throw new WebApplicationException(CONFLICT);
				}
			}
		}
	}
	
	private void checkUniqueIdentifier(IdentifierLocation location, String newIdentifier) throws IOException {
		
		if (isNotEmpty(newIdentifier)) {
			for (String identifier : splitIdentifiers(newIdentifier)) {
				Collection<Bunny> bunnies = registry.findBunnies(byExactIdentifier(location, identifier));
				if (bunnies.size() > 0) {
					throw new WebApplicationException(CONFLICT);
				}
			}
		}
	}
	
	private static Gender toGender(GenderEnum g) {
		if (g != null) {
			switch (g) {
				case FEMALE:
					return Bunny.Gender.FEMALE;
				case MALE:
					return Bunny.Gender.MALE;
				case UNKNOWN:
					return null;
			}
		}
		return null;
	}
	
	private static GenderEnum toGender(Gender g) {
		if (g != null) {
			switch (g) {
				case FEMALE:
					return GenderEnum.FEMALE;
				case MALE:
					return GenderEnum.MALE;
			}
		}
		return GenderEnum.UNKNOWN;
	}

	private static void update(Owner owner, OwnerDTO dto) {
		ofNullable(dto.getEmail()).ifPresent(owner::setEmail);
		ofNullable(dto.getAddress()).ifPresent(owner::setAddress);
		ofNullable(dto.getPhone()).ifPresent(owner::setPhone);
		ofNullable(dto.getName()).ifPresent(owner::setName);
		ofNullable(dto.getPublicOwner()).ifPresent(owner::setPublicOwner);
		ofNullable(dto.getBreederName()).ifPresent(owner::setBreederName);
		ofNullable(dto.getBreederEmail()).ifPresent(owner::setBreederEmail);
		ofNullable(dto.getPublicBreeder()).ifPresent(owner::setPublicBreeder);
	}

	@Override
	public OwnerDTO updateOwner(String id, OwnerDTO ownerDTO) {
		return process(() -> {
			
			validateSession(id);
			
			if (ownerDTO.getId() != null &&
					!ownerDTO.getId().equals(id)) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			Owner owner = validateOwner(id, true);
			
			update(owner, ownerDTO);
			registry.update(owner);
			return toDTO(owner);
		});
	}

	@Override
	public OwnerDTO createOwner(CreateOwnerDTO creationDTO) {
		return process(() -> {
			
			if (getSession() != null) {
				throw new WebApplicationException(UNAUTHORIZED);								
			}
			
			if (isBlank(creationDTO.getUserName())) {
					throw new WebApplicationException(BAD_REQUEST);
			} else if (registry.findOwners(byUserName(creationDTO.getUserName())).size() > 0) {
				throw new WebApplicationException(CONFLICT);
			}
			
			if (isBlank(creationDTO.getPassword())) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			Owner owner = Owner.newOwner()
					.setUserName(creationDTO.getUserName())
					.setPassword(creationDTO.getPassword());
			registry.add(owner);
			
			return toDTO(owner);
		});
	}
	
	@Override
	public OwnerDTO activateOwner(String id, CreateOwnerDTO creationDTO) {
		return process(() -> {
			
			if (getSession() != null) {
				throw new WebApplicationException(UNAUTHORIZED);								
			}
			
			Owner owner = validateOwner(id, false);
			if (isNotEmpty(owner.getUserName())) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			if (isBlank(creationDTO.getUserName())) {
				throw new WebApplicationException(BAD_REQUEST);
			} else if (registry.findOwners(byUserName(creationDTO.getUserName())).size() > 0) {
				throw new WebApplicationException(CONFLICT);
			}
			
			if (isBlank(creationDTO.getPassword())) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			registry.update(owner
					.setUserName(creationDTO.getUserName())
					.setPassword(creationDTO.getPassword()));
			
			return toDTO(owner);
		});
	}

	@Override
	public BunnyDTO reclaimBunny(String id) {
		return process(() -> {

			if (getSession() == null) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			Bunny bunny = validateBunny(id);
			
			if (bunny.getPreviousOwner() == null) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			validateSession(bunny.getPreviousOwner());
			
			Owner currentOwner = validateOwner(bunny.getOwner(), false);
			
			if (currentOwner.isActivated()) {
				throw new WebApplicationException(CONFLICT);
			}
			
			bunny.setOwner(bunny.getPreviousOwner())
				.setPreviousOwner(null);
			registry.update(bunny);
			
			registry.remove(currentOwner);
			
			return toDTO(bunny);
		});
	}

	@Override
	public void deleteBunny(String owner, String bunny) {
		process(() -> {
			
			validateSession(owner);
			validateOwner(owner, false);
			registry.remove(validateBunny(bunny));
			
			return Void.class;
		});
	}

	@Override
	public void deleteOwner(String id) {
		process(() -> {
			
			validateSession(id);
			Owner owner = validateOwner(id, false);
			if (registry.findBunnies(byOwner(id)).size() > 0) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			for (Bunny bunny : registry.findBunnies(byBreeder(id))) {
				registry.update(bunny.setBreeder(null));
			}
			
			registry.remove(owner);
			removeCookie();
			
			return Void.class;
		});
	}

	@Override
	public void approveOwner(String id) {
		process(() -> {
			
			String session = validateSession(id);
			Owner owner = validateOwner(id, false);
			
			if (!owner.isApproved()) {
				Signing signing = sessions.getAttribute(session, SESSION_SIGNING);
				
				if (signing == null) {
					if (isNotEmpty(approvalUrl)) {
						signing = signingService.startSigning(new URL(approvalUrl));
						sessions.setAttribute(session, SESSION_SIGNING, signing);
						redirect(signing.getTransactionUrl());
					} else {
						registry.update(owner.setSignature("-"));
					}
				} else {
					Optional<Boolean> signed = signingService.checkSigning(signing.getToken());
					if (signed.isPresent()) {
						if (signed.get()) {
							Signature signature = signingService.getSignature(signing.getToken());
							registry.update(owner.setSignature(approvalUrl + " (" + signature.getSubject() + "@" + timestamp() + ") "+signature.getSignature()));							
						} else {
							throw new WebApplicationException(NO_CONTENT);
						}
					} else {
						redirect(signing.getTransactionUrl());
						return Void.class;
					}
				}
			}
			
			response.setStatus(OK.getStatusCode());
			return Void.class;
		});
	}

	private void redirect(String url) {
		response.setHeader("Location", url);
		response.setStatus(TEMPORARY_REDIRECT.getStatusCode());
	}
	
	private static String timestamp() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
	}

	@Override
	public void unapproveOwner(String id) {
		process(() -> {
			
			validateSession(id);
			Owner owner = validateOwner(id, true);

			registry.update(owner.unapprove());
			
			return Void.class;
		});
	}

	@Override
	public void deactivateOwner(String id) {
		process(() -> {
			
			validateSession(id);
			Owner owner = validateOwner(id, false);

			if (owner.isActivated()) {
				registry.update(owner.deactivate());
			}
			removeCookie();
			
			return Void.class;
		});
	}

	@Override
	public BunnyDTO claimBunny(String owner, BunnyClaimDTO claim) {
		return process(() -> {
			
			validateSession(owner);
			Owner newOwner = validateOwner(owner, true);
			Owner tempOwner = validateOwner(claim.getClaimToken(), false);
			if (tempOwner.isActivated()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			Collection<Bunny> bunnies = registry.findBunnies(byOwner(tempOwner.getId()));
			if (bunnies.isEmpty()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			Bunny bunny = bunnies.iterator().next();
			
			bunny.setOwner(newOwner.getId());
			registry.update(bunny);
			registry.remove(tempOwner);
			
			return toDTO(bunny);
		});
	}

	@Override
	public BunnyTransferDTO transferBunny(String ownerId, String bunnyId) {
		return process(() -> {
			
			validateSession(ownerId);
			validateOwner(ownerId, true);
			
			Bunny bunny = validateBunny(bunnyId);
			if (!bunny.getOwner().equals(ownerId)) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			String newOwnerId = registry.add(newOwner());
			bunny.setPreviousOwner(ownerId);
			bunny.setOwner(newOwnerId);
			registry.update(bunny);
			
			BunnyTransferDTO dto = new BunnyTransferDTO();
			dto.setClaimToken(newOwnerId);
			return dto;
		});
	}

	@Override
	public void signOffline(String token, OfflineSignatureDTO dto) {
		process(() -> {
			signingService.setSigningState(token, dto.getSubject(), dto.getSuccess());
			return Void.class;
			});
	}

	@Override
	public void recoverOwner(String userName, RecoveryDTO recoveryDTO) {
		process(() -> {
			
			if (getSession() != null) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			Collection<Owner> owners = registry.findOwners(byUserName(userName));
			if (owners.isEmpty()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			Owner owner = owners.iterator().next();
			
			List<BunnyIdentifierDTO> bunnyIdentifierList = recoveryDTO.getBunnyIdentifier();
			if (CollectionUtils.isEmpty(bunnyIdentifierList)) {
				throw new WebApplicationException(BAD_REQUEST);				
			}
			
			Map<String, Predicate<String>> filter = new HashMap<>();
			for (BunnyIdentifierDTO bunnyIdentifier : bunnyIdentifierList) {
				if (bunnyIdentifier.getLocation() == null ||
						bunnyIdentifier.getIdentifier() == null) {
					throw new WebApplicationException(BAD_REQUEST);				
				}
				filter.putAll(byExactIdentifier(location(bunnyIdentifier.getLocation()), bunnyIdentifier.getIdentifier()));
			}
			
			Collection<Bunny> bunnies = registry.findBunnies(filter);
			if (bunnies.size() != 1) {
				throw new WebApplicationException(NOT_FOUND);
			}
			Bunny bunny = bunnies.iterator().next();
			
			if (!bunny.getOwner().equals(owner.getId())) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			if (isAllBlank(recoveryDTO.getNewPassword())) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			owner.setPassword(recoveryDTO.getNewPassword());
			registry.update(owner);
			
			return Void.class;
		});
	}

	private static Bunny.IdentifierLocation location(BunnyIdentifierLocation location) {
		switch (location) {
			case LEFT_EAR:
				return Bunny.IdentifierLocation.LEFT_EAR;
			case RIGHT_EAR:
				return Bunny.IdentifierLocation.RIGHT_EAR;
			case CHIP:
				return Bunny.IdentifierLocation.CHIP;
			case RING:
				return Bunny.IdentifierLocation.RING;
			default:
				throw new WebApplicationException(location.toString(), BAD_REQUEST);
		}
	}
}
