package org.keycloak.dashboard.beans.filters;

class ReadyFilter extends LabelFilter {

    public ReadyFilter(boolean include) {
        super("status/ready", include);
    }

}
