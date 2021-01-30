package se.skvf.kaninregister.api;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.lang3.StringUtils.isAllBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.skvf.kaninregister.data.Table.ALL;
import static se.skvf.kaninregister.model.Bunny.byOwner;
import static se.skvf.kaninregister.model.Owner.byUserName;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.skvf.kaninregister.data.Table;
import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;
import se.skvf.kaninregister.model.Registry;

@Provider
@Path("api")
public class BunnyRegistryApiImpl implements BunnyRegistryApi {

	private static final Log LOG = LogFactory.getLog(BunnyRegistryApiImpl.class);
	
	@Autowired
	private Registry registry;
	
	@Autowired
	private SessionManager sessions;
	
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
			validateOwner(ownerId);
			
			bunnyDTO.setOwner(ownerId);
			bunnyDTO.setId(registry.add(toBunny(bunnyDTO)));
			return bunnyDTO;
			
		});
	}

	private <T> T process(Callable<T> call) {
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
		dto.setOwner(bunny.getOwner());
		return dto;
	}
	
	private static OwnerDTO toDTO(Owner owner) {
		OwnerDTO dto = new OwnerDTO();
		dto.setId(owner.getId());
		dto.setFirstName(owner.getFirstName());
		dto.setLastName(owner.getLastName());
		dto.setEmail(owner.getEmail());
		dto.setUserName(owner.getUserName());
		return dto;
	}
	
	private static OwnerListDTO toListDTO(Owner owner) {
		OwnerListDTO dto = new OwnerListDTO();
		dto.setId(owner.getId());
		dto.setFirstName(owner.getFirstName());
		dto.setLastName(owner.getLastName());
		return dto;
	}
	
	private static Owner toOwner(OwnerDTO dto) {
		return new Owner().setId(dto.getId())
				.setFirstName(dto.getFirstName())
				.setLastName(dto.getLastName())
				.setEmail(dto.getEmail())
				.setUserName(dto.getUserName());
	}

	private String getSession() {
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals(getClass().getSimpleName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
	
	private void setSession(String sessionId) {
		response.addCookie(new Cookie(getClass().getSimpleName(), sessionId));
	}
	
	private void validateSession(String ownerId) {
		if (!sessions.isSession(getSession(), ownerId)) {
			throw new WebApplicationException(UNAUTHORIZED);
		}
	}

	@Override
	public OwnerDTO createOwner(OwnerDTO ownerDTO) {
		return process(() -> {
			
			if (ownerDTO.getId() != null) {
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
			
			Owner breeder = validateOwner(validateBunny(id).getBreeder());
			if (breeder.isNotPublicBreeder()) {
				throw new WebApplicationException(NO_CONTENT);
			}
			return toDTO(breeder);
		});
	}

	private Owner validateOwner(String id) throws IOException {
		if (isBlank(id)) {
			throw new WebApplicationException(NOT_FOUND);
		}
		Collection<Owner> owners = registry.findOwners(singleton(id));
		if (owners.isEmpty()) {
			throw new WebApplicationException(NOT_FOUND);
		}
		return owners.iterator().next();
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
			
			Owner owner = validateOwner(validateBunny(id).getOwner());
			if (owner.isNotPublicOwner()) {
				throw new WebApplicationException(NO_CONTENT);
			}
			return toDTO(owner);
		});
	}

	@Override
	public OwnerDTO getOwner(String id) {
		return process(() -> {
			return toDTO(validateOwner(id));
		});
	}

	@Override
	public BunnyList getOwnerBunnies(String id) {
		return process(() -> {
			
			validateSession(id);
			validateOwner(id);
			
			return toBunnyList(registry.findBunnies(byOwner(id)));
		});
	}

	private static BunnyList toBunnyList(Collection<Bunny> bunnies) {
		BunnyList list = new BunnyList();
		list.setBunnies(bunnies.stream().map(BunnyRegistryApiImpl::toListDTO).collect(Collectors.toList()));
		return list;
	}
	
	private static OwnerList toOwnerList(Collection<Owner> owners) {
		OwnerList list = new OwnerList();
		list.setOwners(owners.stream().map(BunnyRegistryApiImpl::toListDTO).collect(Collectors.toList()));
		return list;
	}

	@Override
	public OwnerList getOwners() {
		return process(() -> {
			return toOwnerList(registry.findOwners(ALL));
		});
	}

	@Override
	public OwnerDTO login(LoginDTO loginDTO) {
		return process(() -> {
			
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
	}

	@Override
	public void setPassword(String ownerId, PasswordDTO passwordDTO) {
		process(() -> {
			
			if (isAllBlank(passwordDTO.getNewPassword())) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			Owner owner = validateOwner(ownerId);
			
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
			
			Set<String> ownerIds = new HashSet<>();
			ownerIds.add(ownerId);
			if (bunnyDTO.getBreeder() != null) {
				ownerIds.add(bunnyDTO.getBreeder());
			}
			if (isNotEmpty(bunnyDTO.getOwner())) {
				ownerIds.add(bunnyDTO.getOwner());
			}
			
			Map<String, Owner> owners = registry.findOwners(ownerIds).stream().collect(toMap(Owner::getId, identity()));
			if (owners.get(ownerId) == null) {
				throw new WebApplicationException(NOT_FOUND);
			}
			if (bunnyDTO.getBreeder() != null &&
					owners.get(bunnyDTO.getBreeder()) == null) {
				throw new WebApplicationException(NOT_FOUND);
			}
			if (bunnyDTO.getOwner() != null) {
				if (bunnyDTO.getOwner().isEmpty()) {
					// Anonymous new owner
					bunnyDTO.setOwner(registry.add(new Owner()
							.setFirstName("Ny")
							.setLastName("Ägare")
							.setPublicOwner(false)));
				} else if (owners.get(bunnyDTO.getOwner()) == null) {
					throw new WebApplicationException(NOT_FOUND);
				}
			}
			
			if (bunnyDTO.getId() != null &&
					!bunnyDTO.getId().equals(bunnyId)) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			Bunny bunny = validateBunny(bunnyId);
			
			if (bunnyDTO.getOwner() != null && 
					!bunny.getOwner().equals(bunnyDTO.getOwner())) {
				bunny.setPreviousOwner(bunny.getOwner());
			}
			update(bunny, bunnyDTO);
			registry.update(bunny);
			return toDTO(bunny);
		});
	}

	private static void update(Bunny bunny, BunnyDTO dto) {
		ofNullable(dto.getBreeder()).ifPresent(bunny::setBreeder);
		ofNullable(dto.getName()).ifPresent(bunny::setName);
		ofNullable(dto.getOwner()).ifPresent(bunny::setOwner);
	}
	
	private static void update(Owner owner, OwnerDTO dto) {
		ofNullable(dto.getEmail()).ifPresent(owner::setEmail);
		ofNullable(dto.getFirstName()).ifPresent(owner::setFirstName);
		ofNullable(dto.getLastName()).ifPresent(owner::setLastName);
		ofNullable(dto.getUserName()).ifPresent(owner::setUserName);
	}

	@Override
	public OwnerDTO updateOwner(String id, OwnerDTO ownerDTO) {
		return process(() -> {
			
			validateSession(id);
			
			if (ownerDTO.getId() != null &&
					!ownerDTO.getId().equals(id)) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			Owner owner = validateOwner(id);
			
			update(owner, ownerDTO);
			registry.update(owner);
			return toDTO(owner);
		});
	}

	@Override
	public void activate(String id, ActivationDTO activationDTO) {
		process(() -> {
			
			Owner owner = validateOwner(id);
			
			if (owner.isActivated()) {
				throw new WebApplicationException(UNAUTHORIZED);				
			}
			
			if (activationDTO.getBunny() != null) {
				Bunny bunny = validateBunny(activationDTO.getBunny());
				if (!bunny.getOwner().equals(owner.getId())) {
					throw new WebApplicationException(BAD_REQUEST);
				}
			}
			if (owner.getUserName() != null &&
					!owner.getUserName().equals(activationDTO.getUserName())) {
				throw new WebApplicationException(BAD_REQUEST);
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
	public BunnyDTO revertBunnyOwner(String id) {
		return process(() -> {

			if (getSession() == null) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			Bunny bunny = validateBunny(id);
			
			if (bunny.getPreviousOwner() == null) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			
			validateSession(bunny.getPreviousOwner());
			
			Owner currentOwner = validateOwner(bunny.getOwner());
			
			if (currentOwner.isActivated()) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			bunny.setOwner(bunny.getPreviousOwner())
				.setPreviousOwner(null);
			registry.update(bunny);
			
			registry.remove(currentOwner);
			
			return toDTO(bunny);
		});
	}

}
