package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static javax.ws.rs.core.Response.Status.TOO_MANY_REQUESTS;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.lang3.StringUtils.isAllBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
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
import static se.skvf.kaninregister.model.Owner.otherByUserName;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import se.skvf.kaninregister.addo.AddoSigningService;
import se.skvf.kaninregister.addo.Signature;
import se.skvf.kaninregister.addo.Signing;
import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Bunny.Gender;
import se.skvf.kaninregister.model.Bunny.IdentifierLocation;
import se.skvf.kaninregister.model.Owner;
import se.skvf.kaninregister.model.Registry;

@Provider
public class BunnyRegistryApiImpl implements BunnyRegistryApi {

	static final String TRANSFER_OWNER = "Ägarbyte";

	private static final Log LOG = LogFactory.getLog(BunnyRegistryApiImpl.class);

	static final String SESSION_SIGNING = "signing";
	
	@Autowired
	private Registry registry;
	@Autowired
	private SessionManager sessions;
	@Autowired
	private AddoSigningService signingService;
	
	
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
			
			validateBreeder(bunnyDTO.getBreeder(), ownerId, null);
			
			bunnyDTO.setOwner(ownerId);
			Bunny bunny = toBunny(bunnyDTO);
			bunnyDTO.setId(registry.add(bunny));
			return bunnyDTO;
		});
	}

	private void validateBreeder(String newBreederId, String ownerId, String currentBreederId) throws IOException {
		if (isNotEmpty(newBreederId) &&
				!newBreederId.equals(ownerId) &&
				!newBreederId.equals(currentBreederId)) {
			throw new WebApplicationException(BAD_REQUEST);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T process(Callable<T> call) {
		try {
			return call.call();
		} catch (GoogleJsonResponseException g) {
			return (T) logAndThrow(g);
		} catch (WebApplicationException e) {
			LOG.info("Application error", e);
			throw e;
		} catch (IllegalStateException e) {
			return (T) logAndThrow(e, "Application", BAD_REQUEST);	
		} catch (Exception e) {
			return (T) logAndThrow(e, "Unexpected", INTERNAL_SERVER_ERROR);	
		}
	}

	private static Void logAndThrow(Exception e, String error, Status status) {
		LOG.error(error + " error", e);
		throw new WebApplicationException(e, status);
	}
	
	private static Void logAndThrow(GoogleJsonResponseException g) {
		LOG.info("Google error", g);
		if (g.getStatusCode() == TOO_MANY_REQUESTS.getStatusCode()) {
			throw new WebApplicationException(g, TOO_MANY_REQUESTS);	
		} else {
			throw new WebApplicationException(g, INTERNAL_SERVER_ERROR);
		}
	}
	
	private Bunny toBunny(BunnyDTO dto) throws IOException {
		
		checkUniqueIdentifier(CHIP, dto.getChip());
		checkUniqueIdentifier(RING, dto.getRing());
		
		if (dto.getGender() == null) {
			dto.setGender(BunnyGender.UNKNOWN);
		}
		
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
		dto.setPicture(bunny.getPicture());
		dto.setChip(bunny.getChip());
		dto.setLeftEar(bunny.getLeftEar());
		dto.setRightEar(bunny.getRightEar());
		dto.setRing(bunny.getRing());
		dto.setGender(toGender(bunny.getGender()));
		dto.setNeutered(bunny.isNeutered());
		dto.setCoat(bunny.getCoat());
		dto.setColourMarkings(bunny.getColourMarkings());
		dto.setRace(bunny.getRace());
		dto.setFeatures(bunny.getFeatures());
		return dto;
	}
	
	private static OwnerBunnyListDTO toOwnerListDTO(Bunny bunny) {
		OwnerBunnyListDTO dto = new OwnerBunnyListDTO();
		dto.setId(bunny.getId());
		dto.setName(bunny.getName());
		dto.setPicture(bunny.getPicture());
		dto.setChip(bunny.getChip());
		dto.setLeftEar(bunny.getLeftEar());
		dto.setRightEar(bunny.getRightEar());
		dto.setRing(bunny.getRing());
		dto.setGender(toGender(bunny.getGender()));
		dto.setNeutered(bunny.isNeutered());
		dto.setCoat(bunny.getCoat());
		dto.setColourMarkings(bunny.getColourMarkings());
		dto.setRace(bunny.getRace());
		dto.setFeatures(bunny.getFeatures());
		dto.setBirthDate(bunny.getBirthDate());
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
		dto.setBreederPhone(owner.getBreederPhone());
		dto.setBreederAddress(owner.getBreederAddress());
		dto.setPublicBreeder(owner.isPublicBreeder());
		dto.setApproved(owner.isApproved());
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
		dto.setPhone(ofNullable(owner.getBreederPhone())
				.orElse(owner.getPhone()));
		dto.setAddress(ofNullable(owner.getBreederAddress())
				.orElse(owner.getAddress()));
		return dto;
	}
	
	private String getSession() {
		return ofNullable(getPrivateCookie())
				.map(Cookie::getValue)
				.orElse(null);
	}
	
	private Cookie getPrivateCookie() {
		return getCookie(BunnyRegistryApiImpl.class);
	}
	
	private Cookie getPublicCookie() {
		return getCookie(BunnyRegistryApi.class);
	}
	
	private Cookie getCookie(Class<?> name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name.getSimpleName())) {
					return cookie;
				}
			}
		}
		return null;
	}
	
	private void setCookies(String sessionId) {
		Cookie privateCookie = new Cookie(BunnyRegistryApiImpl.class.getSimpleName(), sessionId);
		privateCookie.setHttpOnly(true);
		privateCookie.setPath("/");
		privateCookie.setMaxAge(-1);
		response.addCookie(privateCookie);
		Cookie publicCookie = new Cookie(BunnyRegistryApi.class.getSimpleName(), BunnyRegistryApi.class.getSimpleName());
		publicCookie.setHttpOnly(false);
		publicCookie.setPath(privateCookie.getPath());
		publicCookie.setMaxAge(privateCookie.getMaxAge());
		response.addCookie(publicCookie);
	}
	
	private String validateSession(String ownerId) {
		String session = getSession();
		if (ownerId == null) {
			if (session == null) {
				throw new WebApplicationException(UNAUTHORIZED);				
			}
		} else if (!sessions.isSession(session, ownerId)) {
			if (session != null) {
				removeCookies();
			}
			throw new WebApplicationException(UNAUTHORIZED);
		}
		return session;
	}

	@Override
	public BunnyList findBunnies(List<BunnyIdentifierLocation> locations, List<String> identifiers) {
		return process(() -> {

			validateFilters(locations, identifiers);
			validateWildcards(identifiers);
			
			Collection<Bunny> bunnies = registry.findBunnies(mapFilters(locations, identifiers));
			
			if (bunnies.size() > 10) {
				throw new WebApplicationException(NO_CONTENT);
			}
			
			return toBunnyList(bunnies);
		});
	}

	private void validateFilters(List<BunnyIdentifierLocation> locations, List<String> identifiers) {
		if (isEmpty(locations) || 
				isEmpty(identifiers) ||
				locations.size() != identifiers.size()) {
			throw new WebApplicationException(BAD_REQUEST);
		}
	}

	private Map<String, Predicate<String>> mapFilters(List<BunnyIdentifierLocation> locations, List<String> identifiers)
			throws IOException {
		Map<String, Predicate<String>> filter = new HashMap<>();
		for (int i=0;i<locations.size();i++) {
			filter.putAll(byWildcardIdentifier(location(locations.get(i)), identifiers.get(i)));
		}
		return filter;
	}

	private void validateWildcards(List<String> identifiers) {
		identifiers.forEach(identifier -> {
			long wildcardCount = identifier.chars()
					.filter(c -> c == WILDCARD)
					.count();
			if (wildcardCount > 2) {
				throw new WebApplicationException(BAD_REQUEST);
			}
		});
	}

	@Override
	public BunnyDTO getBunny(String id) {
		return process(() -> toDTO(validateBunny(id)));
	}

	@Override
	public BunnyBreederDTO getBunnyBreeder(String id) {
		return process(() -> {
			
			Bunny bunny = validateBunny(id);
			
			if (StringUtils.isEmpty(bunny.getBreeder())) {
				return new BunnyBreederDTO();
			}
			return toBreederDTO(findPublicBreeder(bunny.getBreeder()));
		});
	}

	private Owner findPublicBreeder(String id) throws IOException {
		Owner breeder = validateOwner(id, false);
		if (breeder.isNotPublicBreeder()) {
			throw new WebApplicationException(NO_CONTENT);
		}
		return breeder;
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
			throw new WebApplicationException(PRECONDITION_FAILED);
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
		return process(() -> toOwnerDTO(findPublicOwner(id)));
	}

	private Owner findPublicOwner(String bunnyId) throws IOException {
		Owner owner = validateOwner(validateBunny(bunnyId).getOwner(), false);
		if (owner.isNotPublicOwner()) {
			throw new WebApplicationException(NO_CONTENT);
		}
		return owner;
	}
	
	@Override
	public BunnyOwnerDTO getBunnyPreviousOwner(String id) {
		return process(() -> {
			
			Bunny bunny = validateBunny(id);
			if (bunny == null) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			if (StringUtils.isEmpty(bunny.getPreviousOwner())) {
				return new BunnyOwnerDTO();
			}
			return toOwnerDTO(findPublicPreviousOwner(bunny));
		});
	}

	private Owner findPublicPreviousOwner(Bunny bunny) throws IOException {
		Owner previousOwner = validateOwner(bunny.getPreviousOwner(), false);
		if (previousOwner.isNotPublicOwner()) {
			throw new WebApplicationException(NO_CONTENT);
		}
		return previousOwner;
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
	public OwnerBunnyList getOwnerBunnies(String id) {
		return process(() -> {
			
			validateSession(id);
			validateOwner(id, false);
			
			OwnerBunnyList bunnies = toOwnerBunnyList(registry.findBunnies(byOwner(id)));
			addTransferBunnies(bunnies, id);
			bunnies.getBunnies().sort(comparing(OwnerBunnyListDTO::getName));
			return bunnies;
		});
	}

	private void addTransferBunnies(OwnerBunnyList bunnies, String id) throws IOException {
		Collection<Bunny> transfers = registry.findBunnies(byPreviousOwner(id));
		if (transfers.size() > 0) {
			Set<String> newOwners = transfers.stream().map(Bunny::getOwner).collect(toSet());
			Map<String, Owner> owners = registry.findOwners(newOwners).stream().collect(toMap(Owner::getId, identity()));
			for (Bunny transfer : transfers) {
				if (isTransferOwner(owners.get(transfer.getOwner()))) {
					OwnerBunnyListDTO dto = toOwnerListDTO(transfer);
					dto.setClaimToken(transfer.getOwner());
					bunnies.getBunnies().add(dto);
				}
			}
		}
	}

	private static BunnyList toBunnyList(Collection<Bunny> bunnies) {
		BunnyList list = new BunnyList();
		list.setBunnies(bunnies.stream()
				.map(BunnyRegistryApiImpl::toListDTO)
				.collect(toList()));
		return list;
	}
	
	private OwnerBunnyList toOwnerBunnyList(Collection<Bunny> bunnies) {
		OwnerBunnyList list = new OwnerBunnyList();
		list.setBunnies(bunnies.stream()
				.map(BunnyRegistryApiImpl::toOwnerListDTO)
				.collect(toList()));
		return list;
	}

	@Override
	public OwnerDTO session() {
		return process(() -> {
			String session = getSession();
			if (session == null) {
				return null;
			}

			String ownerId = sessions.getOwnerIdForSession(session);
			if (isBlank(ownerId)) {
				return null;
			}

			return toDTO(validateOwner(ownerId, false));
		});
	}

	@Override
	public OwnerDTO login(LoginDTO loginDTO) {
		return process(() -> {

			String session = getSession();
			if (session != null) {
				sessions.endSession(session);
			}

			if (loginDTO.getUserName() == null) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			Owner owner = findOwnerByName(loginDTO.getUserName(), UNAUTHORIZED);
			
			if (!owner.validate(loginDTO.getPassword())) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			setCookies(sessions.startSession(owner.getId()));
			return toDTO(owner);
		});
	}

	@Override
	public void logout() {
		sessions.endSession(getSession());
		removeCookies();
	}

	private void removeCookies() {
		Stream.of(getPublicCookie(), getPrivateCookie())
			.filter(Objects::nonNull)
			.forEach(c -> {
				c.setMaxAge(0);
				c.setPath("/");
				response.addCookie(c);
			});
	}

	@Override
	public void changePassword(String ownerId, PasswordDTO passwordDTO) {
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
			
			validateBreeder(bunnyDTO.getBreeder(), ownerId, bunny.getBreeder());
			
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
	
	private static Gender toGender(BunnyGender g) {
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
	
	private static BunnyGender toGender(Gender g) {
		if (g != null) {
			switch (g) {
				case FEMALE:
					return BunnyGender.FEMALE;
				case MALE:
					return BunnyGender.MALE;
			}
		}
		return BunnyGender.UNKNOWN;
	}
	
	private static void update(Owner owner, OwnerDTO dto) {
		ofNullable(dto.getUserName()).ifPresent(owner::setUserName);
		ofNullable(dto.getEmail()).ifPresent(owner::setEmail);
		ofNullable(dto.getAddress()).ifPresent(owner::setAddress);
		ofNullable(dto.getPhone()).ifPresent(owner::setPhone);
		ofNullable(dto.getName()).ifPresent(owner::setName);
		ofNullable(dto.getPublicOwner()).ifPresent(owner::setPublicOwner);
		ofNullable(dto.getBreederName()).ifPresent(owner::setBreederName);
		ofNullable(dto.getBreederEmail()).ifPresent(owner::setBreederEmail);
		ofNullable(dto.getBreederPhone()).ifPresent(owner::setBreederPhone);
		ofNullable(dto.getBreederAddress()).ifPresent(owner::setBreederAddress);
		ofNullable(dto.getPublicBreeder()).ifPresent(owner::setPublicBreeder);
	}

	@Override
	public OwnerDTO updateOwner(String id, OwnerDTO ownerDTO) {
		return process(() -> {
			
			validateSession(id);
			
			Owner owner = findOwner(id, ownerDTO);
			
			validateNewUser(ownerDTO.getUserName(), owner);
			
			update(owner, ownerDTO);
			registry.update(owner);
			return toDTO(owner);
		});
	}

	private Owner findOwner(String id, OwnerDTO ownerDTO) throws IOException {
		if (ownerDTO.getId() != null &&
				!ownerDTO.getId().equals(id)) {
			throw new WebApplicationException(BAD_REQUEST);
		}
		Owner owner = validateOwner(id, true);
		return owner;
	}

	private void validateNewUser(String userName, Owner owner) throws IOException {
		if (userName != null) {
			if (isBlank(userName)) {
				throw new WebApplicationException(BAD_REQUEST);
			} else if (registry.findOwners(otherByUserName(owner.getId(), userName)).size() > 0) {
				throw new WebApplicationException(CONFLICT);
			}
		}
	}

	@Override
	public OwnerDTO createOwner(CreateOwnerDTO creationDTO) {
		return process(() -> {
			
			validateNoSession();
			
			validateNewUser(creationDTO.getUserName(), creationDTO.getPassword());
			
			Owner owner = Owner.newOwner()
					.setUserName(creationDTO.getUserName())
					.setPassword(creationDTO.getPassword());
			registry.add(owner);
			
			return toDTO(owner);
		});
	}

	private void validateNoSession() {
		if (getSession() != null) {
			removeCookies();
			throw new WebApplicationException(UNAUTHORIZED);								
		}
	}
	
	@Override
	public OwnerDTO activateOwner(String id, CreateOwnerDTO creationDTO) {
		return process(() -> {
			
			validateNoSession();
			
			Owner owner = getActivationOwner(id);
			
			validateNewUser(creationDTO.getUserName(), creationDTO.getPassword());
			
			registry.update(owner
					.setUserName(creationDTO.getUserName())
					.setPassword(creationDTO.getPassword()));
			
			return toDTO(owner);
		});
	}
	
	@Override
	public ActivatedStatusDTO isOwnerActivated(String id) {
		return process(() -> {
			
			validateNoSession();
			
			Owner owner = validateOwner(id, false);
			if (isTransferOwner(owner)) {
				throw new WebApplicationException(NOT_FOUND);
			}

			ActivatedStatusDTO dto = new ActivatedStatusDTO();
			dto.setActivated(owner.isActivated());
			return dto;
		});
	}

	private void validateNewUser(String userName, String password) throws IOException {
		if (isBlank(userName)) {
			throw new WebApplicationException(BAD_REQUEST);
		} else if (registry.findOwners(byUserName(userName)).size() > 0) {
			throw new WebApplicationException(CONFLICT);
		}
		if (isBlank(password)) {
			throw new WebApplicationException(BAD_REQUEST);
		}
	}

	private Owner getActivationOwner(String id) throws IOException {
		Owner owner = validateOwner(id, false);
		if (isTransferOwner(owner)) {
			throw new WebApplicationException(NOT_FOUND);
		}
		if (owner.isActivated()) {
			throw new WebApplicationException(BAD_REQUEST);
		}
		return owner;
	}

	@Override
	public BunnyDTO reclaimBunny(String id) {
		return process(() -> {

			validateSession(null);
			
			Bunny bunny = validateBunny(id);
			
			if (bunny.getPreviousOwner() == null) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			validateSession(bunny.getPreviousOwner());
			
			Owner currentOwner = validateOwner(bunny.getOwner(), false);
			
			if (!isTransferOwner(currentOwner)) {
				throw new WebApplicationException(CONFLICT);
			}
			
			bunny.setOwner(bunny.getPreviousOwner())
				.setPreviousOwner(null);
			registry.update(bunny);
			
			registry.remove(currentOwner);
			
			return toDTO(bunny);
		});
	}

	private static boolean isTransferOwner(Owner owner) {
		return owner.getName().equals(TRANSFER_OWNER) && !owner.isActivated();
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
			removeCookies();
			
			return Void.class;
		});
	}

	@Override
	public void approveOwner(String id) {
		process(() -> {
			
			String session = validateSession(id);
			Owner owner = validateOwner(id, false);

			boolean redirected = false;
			if (!owner.isApproved()) {
				Signing signing = sessions.getAttribute(session, SESSION_SIGNING);
				
				if (signing == null) {
					redirected = newSigning(session, owner, redirected);
				} else {
					redirected = ongoingSigning(session, owner, redirected, signing);
				}
			}
			
			// Override the Jersey default 204 for void methods
			if (!redirected) {
				response.sendError(OK.getStatusCode(), "");
			}
			return Void.class;
		});
	}

	private boolean ongoingSigning(String session, Owner owner, boolean redirected, Signing signing) throws IOException {
		Optional<Boolean> signed = signingService.checkSigning(signing.getToken());
		if (signed.isPresent()) {
			if (signed.get()) {
				Signature signature = signingService.getSignature(signing.getToken());
				clearSigning(session, signing);
				registry.update(owner.setSignature(signature.getUrl() + " (" + signature.getSubject() + "@" + timestamp() + ") "+signature.getSignature()));							
			} else {
				clearSigning(session, signing);
				throw new WebApplicationException(NO_CONTENT);
			}
		} else {
			redirect(signing.getTransactionUrl());
			redirected = true;
		}
		return redirected;
	}

	private boolean newSigning(String session, Owner owner, boolean redirected) throws IOException {
		Signing signing;
		signing = signingService.startSigning();
		if (signing != null && isNotEmpty(signing.getTransactionUrl())) {
			sessions.setAttribute(session, SESSION_SIGNING, signing);
			redirect(signing.getTransactionUrl());
			redirected = true;
		} else {
			clearSigning(session, signing);
			registry.update(owner.setSignature("-"));
		}
		return redirected;
	}

	private void clearSigning(String session, Signing signing) {
		Optional.ofNullable(signing)
			.map(Signing::getToken)
			.ifPresent(signingService::clearSigning);
		sessions.setAttribute(session, SESSION_SIGNING, null);
	}

	private void redirect(String url) throws IOException {
		response.setHeader("Location", url);
		response.sendError(ACCEPTED .getStatusCode(), "");
	}
	
	private static String timestamp() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
	}

	@Override
	public OwnerDTO unapproveOwner(String id) {
		return process(() -> {
			
			validateSession(id);
			Owner owner = validateOwner(id, true);

			registry.update(owner.unapprove());
			
			return toDTO(owner);
		});
	}

	@Override
	public OwnerDTO deactivateOwner(String id) {
		return process(() -> {
			
			validateSession(id);
			Owner owner = validateOwner(id, false);

			if (owner.isActivated()) {
				registry.update(owner.deactivate());
			}
			removeCookies();
			
			return toDTO(owner);
		});
	}

	@Override
	public BunnyDTO claimBunny(String owner, BunnyClaimDTO claim) {
		return process(() -> {
			
			validateSession(owner);
			Owner newOwner = validateOwner(owner, true);
			Owner currentOwner = validateOwner(claim.getClaimToken(), false);
			if (!isTransferOwner(currentOwner)) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			Bunny bunny = findBunny(currentOwner);
			
			bunny.setOwner(newOwner.getId());
			registry.update(bunny);
			registry.remove(currentOwner);
			
			return toDTO(bunny);
		});
	}

	private Bunny findBunny(Owner currentOwner) throws IOException {
		Collection<Bunny> bunnies = registry.findBunnies(byOwner(currentOwner.getId()));
		if (bunnies.isEmpty()) {
			throw new WebApplicationException(NOT_FOUND);
		}
		Bunny bunny = bunnies.iterator().next();
		return bunny;
	}

	@Override
	public BunnyTransferDTO transferBunny(String ownerId, String bunnyId) {
		return process(() -> {
			
			validateSession(ownerId);
			validateOwner(ownerId, false);
			
			Bunny bunny = validateBunny(bunnyId);
			if (!bunny.getOwner().equals(ownerId)) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			String newOwnerId = registry.add(newOwner().setName(TRANSFER_OWNER));
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
			
			validateNoSession();
			
			if (isAllBlank(recoveryDTO.getNewPassword())) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			Owner owner = findOwnerByName(userName, NOT_FOUND);
			
			Map<String, Predicate<String>> filter = mapFilter(recoveryDTO.getBunnyIdentifiers());
			
			Bunny bunny = findSingleBunny(filter);
			
			if (!bunny.getOwner().equals(owner.getId())) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			owner.setPassword(recoveryDTO.getNewPassword());
			registry.update(owner);
			
			return Void.class;
		});
	}

	private Bunny findSingleBunny(Map<String, Predicate<String>> filter) throws IOException {
		Collection<Bunny> bunnies = registry.findBunnies(filter);
		if (bunnies.size() != 1) {
			throw new WebApplicationException(NOT_FOUND);
		}
		Bunny bunny = bunnies.iterator().next();
		return bunny;
	}

	private Map<String, Predicate<String>> mapFilter(List<BunnyIdentifierDTO> bunnyIdentifierList) throws IOException {
		if (isEmpty(bunnyIdentifierList)) {
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
		return filter;
	}

	private Owner findOwnerByName(String userName, Status status) throws IOException {
		Collection<Owner> owners = registry.findOwners(byUserName(userName));
		if (owners.isEmpty()) {
			throw new WebApplicationException(status);
		}
		Owner owner = owners.iterator().next();
		return owner;
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
