# Ubuntu linux, nagios, filebeat, and Oracle Java 8
FROM $TODO_DOCKER_IMAGE_LOCATION
MAINTAINER CodeArcs [...]

# Update the Nagios configuration
ADD nagios/nagios.cfg /opt/nagios/etc/nagios.cfg

# Update Apache2 configuration files
ADD apache2/apache2.conf /etc/apache2/apache2.conf
ADD apache2/ports.conf /etc/apache2/ports.conf

# Create the username/password for Nagios website and start Apache2
RUN htpasswd -bc /opt/nagios/etc/htpasswd.users $TODO_NAGIOS_ADMIN $TODO_NAGIOS_PASSWORD &&\ 
	chown nagios.nagios /opt/nagios/etc/htpasswd.users

# Add the Candlestack files
ADD candlestack /opt/candlestack
ADD start-candlestack.sh /opt/
ADD scripts /opt/scripts

# Add the filebeat files
ADD start-filebeat.sh /opt/
ADD *-filebeat.yml /opt/filebeat/

# Add the metricbeat files
ADD start-metricbeat.sh /opt/
ADD *-metricbeat.yml /opt/metricbeat/

# Create log directories
RUN mkdir /var/log/filebeat/ &&\
	mkdir /var/log/metricbeat/ &&\
	mkdir /var/log/candlestack/ &&\
	mkdir /var/log/apache/ &&\
	mkdir /var/log/supervisord/

# Add the supervisord files
ADD supervisord.conf /etc/supervisor/supervisord.conf


# Expose and start the container
EXPOSE 9090
CMD supervisord -c /etc/supervisor/supervisord.conf