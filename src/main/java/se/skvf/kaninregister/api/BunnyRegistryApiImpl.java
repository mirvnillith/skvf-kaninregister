package se.skvf.kaninregister.api;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.lang3.StringUtils.isAllBlank;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

import org.springframework.beans.factory.annotation.Autowired;

import se.skvf.kaninregister.data.Table;
import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;
import se.skvf.kaninregister.model.Registry;

public class BunnyRegistryApiImpl implements BunnyRegistryApi {

	@Autowired
	private Registry registry;
	
	@Autowired
	private SessionManager sessions;
	
	@Context
	HttpServletRequest request;
	@Context
	HttpServletResponse response;
	
	@Override
	public BunnyDTO createBunny(String ownerId, BunnyDTO bunnyDTO) {
		return process(() -> {
			
			validateSession(ownerId);
			if (bunnyDTO.getId() != null || 
					(bunnyDTO.getOwner() != null && !bunnyDTO.getOwner().equals(ownerId))) {
				throw new WebApplicationException(BAD_REQUEST);
			}
			if (registry.findOwners(singleton(ownerId)).isEmpty()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			bunnyDTO.setOwner(ownerId);
			bunnyDTO.setId(registry.add(toBunny(bunnyDTO)));
			return bunnyDTO;
			
		});
	}

	private <T> T process(Callable<T> call) {
		try {
			return call.call();
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new WebApplicationException(e, INTERNAL_SERVER_ERROR);
		}
	}
	
	private static Bunny toBunny(BunnyDTO dto) {
		return new Bunny().setId(dto.getId())
				.setName(dto.getName())
				.setOwner(dto.getOwner());
	}
	
	private static BunnyDTO toDTO(Bunny bunny) {
		BunnyDTO dto = new BunnyDTO();
		dto.setId(bunny.getId());
		dto.setName(bunny.getName());
		dto.setOwner(bunny.getOwner());
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
				.setLastName(dto.getLastName());
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BunnyDTO getBunny(String id) {
		return process(() -> {
			
			Collection<Bunny> bunny = registry.findBunnies(singleton(id));
			
			if (bunny.isEmpty()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			return toDTO(bunny.iterator().next());
		});
	}

	@Override
	public OwnerDTO getBunnyBreeder(String id) {
		return process(() -> {
			
			Collection<Bunny> bunny = registry.findBunnies(singleton(id));
			if (bunny.isEmpty()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			Collection<Owner> breeder = registry.findOwners(singleton(bunny.iterator().next().getOwner()));
			if (breeder.isEmpty()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			Owner b = breeder.iterator().next();
			if (b.isNotPublicBreeder()) {
				throw new WebApplicationException(NO_CONTENT);
			}
			return toDTO(b);
		});
	}

	@Override
	public OwnerDTO getBunnyOwner(String id) {
		return process(() -> {
			
			Collection<Bunny> bunny = registry.findBunnies(singleton(id));
			if (bunny.isEmpty()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			Collection<Owner> owner = registry.findOwners(singleton(bunny.iterator().next().getOwner()));
			if (owner.isEmpty()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			Owner o = owner.iterator().next();
			if (o.isNotPublicOwner()) {
				throw new WebApplicationException(NO_CONTENT);
			}
			return toDTO(o);
		});
	}

	@Override
	public OwnerDTO getOwner(String id) {
		return process(() -> {
			
			Collection<Owner> owner = registry.findOwners(singleton(id));
			
			if (owner.isEmpty()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			return toDTO(owner.iterator().next());
		});
	}

	@Override
	public BunnyList getOwnerBunnies(String id) {
		return process(() -> {
			
			validateSession(id);
			
			Collection<Owner> owner = registry.findOwners(singleton(id));
			
			if (owner.isEmpty()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			
			return toBunnyList(registry.findBunnies(Bunny.byOwner(id)));
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
			return toOwnerList(registry.findOwners(Table.ALL));
		});
	}

	@Override
	public void login(LoginDTO loginDTO) {
		process(() -> {
			
			Collection<Owner> owners;
			if (loginDTO.getEmail() != null) {
				
				owners = registry.findOwners(Owner.byEmail(loginDTO.getEmail()));
				
			} else if (loginDTO.getBunny() != null) {
				
				Collection<Bunny> bunnies = registry.findBunnies(singleton(loginDTO.getBunny()));
				if (bunnies.isEmpty()) {
					throw new WebApplicationException(UNAUTHORIZED);
				}
				owners = registry.findOwners(singleton(bunnies.iterator().next().getOwner()));
				
			} else {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			if (owners.isEmpty()) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			Owner owner = owners.iterator().next();
			if (!owner.validate(loginDTO.getPassword())) {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			setSession(sessions.startSession(owner.getId()));
			return Void.class;
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
			
			Collection<Owner> owners = emptyList();
			if (ownerId != null) {
				owners = registry.findOwners(singleton(ownerId));
			}
			if (owners.isEmpty()) {
				throw new WebApplicationException(NOT_FOUND);
			}
			Owner owner = owners.iterator().next();
			
			if (passwordDTO.getOldPassword() != null) {
				
				if (!owner.validate(passwordDTO.getOldPassword())) {
					throw new WebApplicationException(UNAUTHORIZED);
				}
				
			} else if (passwordDTO.getBunny() != null) {
				
				Collection<Bunny> bunnies = registry.findBunnies(singleton(passwordDTO.getBunny()));
				if (bunnies.isEmpty() || 
						!ownerId.equals(bunnies.iterator().next().getOwner())) {
					throw new WebApplicationException(UNAUTHORIZED);
				}
				
			} else {
				throw new WebApplicationException(UNAUTHORIZED);
			}
			
			registry.update(owner.setPassword(passwordDTO.getNewPassword()));
			return Void.class;
		});
	}

	@Override
	public BunnyDTO updateBunny(String ownerId, String bunnyId, BunnyDTO bunnyDTO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OwnerDTO updateOwner(String id, OwnerDTO ownerDTO) {
		// TODO Auto-generated method stub
		return null;
	}

}
