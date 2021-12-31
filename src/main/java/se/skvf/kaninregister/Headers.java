package se.skvf.kaninregister;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Headers implements Filter {

	static final String VARIANT = "skvf-variant";
	static final String FEEDBACK = "skvf-feedback";
	
	@Value("${skvf.variant:}")
	private String variant;
	@Value("${skvf.feedback:kaninregistret@skvf.se}")
	private String feedback;

	void setVariant(String variant) {
		this.variant = variant;
	}
	
	void setFeedback(String feedback) {
		this.feedback = feedback;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		((HttpServletResponse)response).addHeader(FEEDBACK, feedback);
		
		if (StringUtils.isNotEmpty(variant)) {
			((HttpServletResponse)response).addHeader(VARIANT, variant);
		}
		chain.doFilter(request, response);
	}

}
