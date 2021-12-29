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
public class VariantHeader implements Filter {

	static final String HEADER = "skvf-variant";
	
	@Value("${skvf.variant:}")
	private String variant;

	void setVariant(String variant) {
		this.variant = variant;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (StringUtils.isNotEmpty(variant)) {
			((HttpServletResponse)response).addHeader(HEADER, variant);
		}
		chain.doFilter(request, response);
	}

}
