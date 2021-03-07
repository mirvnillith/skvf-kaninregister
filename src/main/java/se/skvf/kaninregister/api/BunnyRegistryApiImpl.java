package se.skvf.kaninregister.api;

import static java.util.Collections.emptyList;
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
import static javax.ws.rs.core.Response.Status.TEMPORARY_REDIRECT;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.lang3.StringUtils.isAllBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.skvf.kaninregister.model.Bunny.byBreeder;
import static se.skvf.kaninregister.model.Bunny.byOwner;
import static se.skvf.kaninregister.model.Bunny.byPreviousOwner;
import static se.skvf.kaninregister.model.Owner.byUserName;
import static se.skvf.kaninregister.model.Owner.newOwner;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import se.skvf.kaninregister.addo.AddoSigningService;
import se.skvf.kaninregister.addo.Signature;
import se.skvf.kaninregister.addo.Signing;
import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;
import se.skvf.kaninregister.model.Registry;

@Provider
@Path("api")
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
		} catch (Exception e) {
			LOG.error("Unexpected error", e);
			throw new WebApplicationException(e, INTERNAL_SERVER_ERROR);
		}
	}
	
	private static Bunny toBunny(BunnyDTO dto) {
		return new Bunny().setId(dto.getId())
				.setName(dto.getName())
				.setOwner(dto.getOwner())
				.setBreeder(dto.getBreeder());
	}
	
	private static BunnyDTO toDTO(Bunny bunny) {
		BunnyDTO dto = new BunnyDTO();
		dto.setId(bunny.getId());
		dto.setName(bunny.getName());
		dto.setOwner(bunny.getOwner());
		dto.setPreviousOwner(bunny.getPreviousOwner());
		dto.setBreeder(bunny.getBreeder());
		return dto;
	}
	
	private static BunnyListDTO toListDTO(Bunny bunny) {
		BunnyListDTO dto = new BunnyListDTO();
		dto.setId(bunny.getId());
		dto.setName(bunny.getName());
		return dto;
	}
	
	private static OwnerDTO toDTO(Owner owner) {
		OwnerDTO dto = new OwnerDTO();
		dto.setId(owner.getId());
		dto.setFirstName(owner.getFirstName());
		dto.setLastName(owner.getLastName());
		dto.setEmail(owner.getEmail());
		dto.setUserName(owner.getUserName());
		dto.setPublicOwner(owner.isPublicOwner());
		dto.setBreeder(owner.isBreeder());
		dto.setBreederName(owner.getBreederName());
		dto.setPublicBreeder(owner.isPublicBreeder());
		return dto;
	}
	
	private static Owner toOwner(OwnerDTO dto) {
		Owner owner= new Owner().setId(dto.getId())
				.setFirstName(dto.getFirstName())
				.setLastName(dto.getLastName())
				.setEmail(dto.getEmail())
				.setUserName(dto.getUserName())
				.setBreederName(dto.getBreederName());
		ofNullable(dto.getPublicOwner()).ifPresent(owner::setPublicOwner);
		ofNullable(dto.getBreeder()).ifPresent(owner::setBreeder);
		ofNullable(dto.getPublicBreeder()).ifPresent(owner::setPublicBreeder);
		return owner;
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
				if (cookie.getName().equals(getClass().getSimpleName())) {
					return cookie;
				}
			}
		}
		return null;
	}
	
	private void setSession(String sessionId) {
		Cookie cookie = new Cookie(getClass().getSimpleName(), sessionId);
		cookie.setSecure(true);
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
	public OwnerDTO createOwner(OwnerDTO ownerDTO) {
		return process(() -> {
			
			if (getSession() != null) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			if (ownerDTO.getId() != null ||
					ownerDTO.getUserName() == null) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			ownerDTO.setId(registry.add(toOwner(ownerDTO)));
			return ownerDTO;
		
		});
	}

	@Override
	public BunnyList findBunnies(String identifier, String name) {
		return process(() -> {

			BunnyList list = new BunnyList();
			list.setBunnies(emptyList());
			return list;
		});
	}

	@Override
	public BunnyDTO getBunny(String id) {
		return process(() -> {
			return toDTO(validateBunny(id));
		});
	}

	@Override
	public OwnerDTO getBunnyBreeder(String id) {
		return process(() -> {
			
			Owner breeder = validateOwner(validateBunny(id).getBreeder(), false);
			if (breeder.isNotPublicBreeder()) {
				throw new WebApplicationException(NO_CONTENT);
			}
			return toDTO(breeder);
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
	public OwnerDTO getBunnyOwner(String id) {
		return process(() -> {
			
			Owner owner = validateOwner(validateBunny(id).getOwner(), false);
			if (owner.isNotPublicOwner()) {
				throw new WebApplicationException(NO_CONTENT);
			}
			return toDTO(owner);
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

	private BunnyList toBunnyList(Collection<Bunny> bunnies) throws IOException {
		BunnyList list = new BunnyList();
		list.setBunnies(bunnies.stream().map(BunnyRegistryApiImpl::toListDTO).collect(Collectors.toList()));
		return list;
	}
	
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
			c.setSecure(true);
			c.setMaxAge(0);
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

	private static void update(Bunny bunny, BunnyDTO dto) {
		ofNullable(dto.getBreeder()).ifPresent(bunny::setBreeder);
		ofNullable(dto.getName()).ifPresent(bunny::setName);
	}
	
	private static void update(Owner owner, OwnerDTO dto) {
		ofNullable(dto.getEmail()).ifPresent(owner::setEmail);
		ofNullable(dto.getFirstName()).ifPresent(owner::setFirstName);
		ofNullable(dto.getLastName()).ifPresent(owner::setLastName);
		ofNullable(dto.getPublicOwner()).ifPresent(owner::setPublicOwner);
		ofNullable(dto.getBreeder()).ifPresent(owner::setBreeder);
		ofNullable(dto.getBreederName()).ifPresent(owner::setBreederName);
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
	public void activateOwner(String id, ActivationDTO activationDTO) {
		process(() -> {
			
			if (getSession() != null) {
				throw new WebApplicationException(UNAUTHORIZED);								
			}
			
			Owner owner = validateOwner(id, false);
			
			if (owner.isActivated()) {
				throw new WebApplicationException(NO_CONTENT);				
			}
			
			if (activationDTO.getBunny() != null) {
				Bunny bunny = validateBunny(activationDTO.getBunny());
				if (!bunny.getOwner().equals(owner.getId())) {
					throw new WebApplicationException(BAD_REQUEST);
				}
			}
			if (owner.getUserName() != null) {
				if (!owner.getUserName().equals(activationDTO.getUserName())) {
					throw new WebApplicationException(BAD_REQUEST);
				}
			} else if (registry.findOwners(byUserName(activationDTO.getUserName())).size() > 0) {
				throw new WebApplicationException(CONFLICT);
			}
			
			if (isAllBlank(activationDTO.getPassword())) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			owner.setUserName(activationDTO.getUserName())
				.setPassword(activationDTO.getPassword());
			registry.update(owner);
			
			return Void.class;
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
					}
				}
			}
			
			return Void.class;
		});
	}

	private void redirect(String url) {
		response.setHeader("Location", url);
		throw new WebApplicationException(TEMPORARY_REDIRECT);
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
}
