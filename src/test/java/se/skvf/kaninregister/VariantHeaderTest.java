package se.skvf.kaninregister;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.skvf.kaninregister.VariantHeader.HEADER;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class VariantHeaderTest extends BunnyTest {

	private VariantHeader header;
	
	@Mock
	private HttpServletResponse response;
	@Mock
	private ServletRequest request;
	@Mock
	private FilterChain chain;
	
	@BeforeEach
	void setup() {
		header = new VariantHeader();
	}
	
	@Test
	void nullVariant() throws IOException, ServletException {
		
		header.doFilter(request, response, chain);
		
		verify(chain).doFilter(request, response);
		verifyNoMoreInteractions(request, response);
	}
	
	@Test
	void emptyVariant() throws IOException, ServletException {
		
		header.setVariant("");
		header.doFilter(request, response, chain);
		
		verify(chain).doFilter(request, response);
		verifyNoMoreInteractions(request, response);
	}
	
	@Test
	void nonEmptyVariant() throws IOException, ServletException {
		
		String variant = randomUUID().toString();
		header.setVariant(variant);
		header.doFilter(request, response, chain);
		
		verify(chain).doFilter(request, response);
		verify(response).addHeader(HEADER, variant);
		verifyNoMoreInteractions(request, response);
	}
}
