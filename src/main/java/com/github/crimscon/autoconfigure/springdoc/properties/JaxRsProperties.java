package com.github.crimscon.autoconfigure.springdoc.properties;

import org.springdoc.core.Constants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.ws.rs.core.Application;

@ConfigurationProperties(prefix = Constants.SPRINGDOC_PREFIX)
@ConditionalOnProperty(name = Constants.SPRINGDOC_ENABLED, matchIfMissing = true)
@ConditionalOnBean(Application.class)
public class JaxRsProperties {
    private JaxRs jaxRs = new JaxRs();

    public JaxRsProperties() {
    }

    public JaxRs getJaxRs() {
        return this.jaxRs;
    }

    public void setJaxRs(JaxRs jaxRs) {
        this.jaxRs = jaxRs;
    }

    private class JaxRs {
        private boolean enabled = true;

        public JaxRs() {
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
