#!/usr/bin/env bash


function update_conf_key_value {
    key="$1"
    value="$2"
    pki_properties_file="$3"

    #Add or update key=value pair
    key_exists=$(grep "${key}" "${pki_properties_file}" | wc -l | xargs)
    if [ "${key_exists}" == "0" ]; then
        echo "${key}=${value}" >> "${pki_properties_file}"
    else
        normalized_key=$(printf '%s' "${key}" | sed -e 's/[]\/$*.^[]/\\&/g');
        normalized_value=$(printf '%s' "${value}" | sed -e 's/[]\/$*.^[]/\\&/g');
        #remove comment if it was commented out
        sed -i "s/^#${normalized_key}/${normalized_key}/g" "${pki_properties_file}"
        #replace value for the specified key
        sed -i "/^${normalized_key}/s/=.*$/=${normalized_value}/" "${pki_properties_file}"
    fi
}


apk add --no-cache pcre-tools

#Remove default deployed applications
rm -rf /srv/tomcat/webapps/ROOT
rm -rf /srv/tomcat/webapps/manager
rm -rf /srv/tomcat/webapps/host-manager

#Fix skipjars setup
catalinaConf="/srv/tomcat/conf/catalina.properties"
catalinaLog="/srv/tomcat/logs/catalina.log"
skipJars="$(tomcatFindSkipJars "${catalinaLog}" | sort | uniq)"
lines=""
while IFS= read -r skipJar; do
	lines="$(printf '%s%s,' "${lines}" "${skipJar}")"
done <<< "${skipJars}"
lines="$(printf '%s\' "${lines}")"
update_conf_key_value "tomcat.util.scan.StandardJarScanFilter.jarsToSkip" "${lines}" "${catalinaConf}"