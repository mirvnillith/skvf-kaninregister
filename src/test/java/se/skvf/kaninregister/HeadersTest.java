package se.skvf.kaninregister;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.skvf.kaninregister.Headers.FEEDBACK;
import static se.skvf.kaninregister.Headers.VARIANT;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class HeadersTest extends BunnyTest {

	private Headers headers;
	
	@Mock
	private HttpServletResponse response;
	@Mock
	private ServletRequest request;
	@Mock
	private FilterChain chain;
	
	@BeforeEach
	void setup() {
		headers = new Headers();
	}
	
	@Test
	void nullVariant() throws IOException, ServletException {
		
		headers.doFilter(request, response, chain);
		
		verify(chain).doFilter(request, response);
		verify(response, never()).addHeader(eq(VARIANT), any());
	}
	
	@Test
	void emptyVariant() throws IOException, ServletException {
		
		headers.setVariant("");
		headers.doFilter(request, response, chain);
		
		verify(chain).doFilter(request, response);
		verify(response, never()).addHeader(eq(VARIANT), any());
	}
	
	@Test
	void nonEmptyVariant() throws IOException, ServletException {
		
		String variant = randomUUID().toString();
		headers.setVariant(variant);
		headers.doFilter(request, response, chain);
		
		verify(chain).doFilter(request, response);
		verify(response).addHeader(VARIANT, variant);
	}
	
	@Test
	void nullFeedback() throws IOException, ServletException {
		
		headers.doFilter(request, response, chain);
		
		verify(chain).doFilter(request, response);
		verify(response).addHeader(FEEDBACK, null);
	}
	
	@Test
	void emptyFeedback() throws IOException, ServletException {
		
		headers.setFeedback("");
		headers.doFilter(request, response, chain);
		
		verify(chain).doFilter(request, response);
		verify(response).addHeader(FEEDBACK, "");
	}
	
	@Test
	void nonEmptyFeedback() throws IOException, ServletException {
		
		String feedback = randomUUID().toString();
		headers.setFeedback(feedback);
		headers.doFilter(request, response, chain);
		
		verify(chain).doFilter(request, response);
		verify(response).addHeader(FEEDBACK, feedback);
	}
}
