# Ubuntu linux, nagios, filebeat, and Oracle Java 8
FROM $TODO_DOCKER_IMAGE_LOCATION
MAINTAINER CodeArcs [...]

# Update the Nagios configuration to look at the directory Candlestack will be updating and have apache listen on the port being exposed by the container
RUN sed -i '55s/.*/cfg_dir=\/var\/tmp\/nagios\/objects/' /opt/nagios/etc/nagios.cfg &&\
	sed -i '5s/.*/Listen 9090/' /etc/apache2/ports.conf

# Create the username/password for Nagios website and start Apache2
RUN htpasswd -bc /opt/nagios/etc/htpasswd.users $TODO_NAGIOS_ADMIN $TODO_NAGIOS_PASSWORD &&\ 
	chown nagios.nagios /opt/nagios/etc/htpasswd.users

ADD candlestack /opt/candlestack

ADD start-candlestack.sh /opt/

ADD start-filebeat.sh /opt/

ADD *-filebeat.yml /opt/filebeat/

ADD supervisord.conf /etc/supervisor/supervisord.conf

EXPOSE 9090

RUN /etc/init.d/apache2 restart

CMD supervisord -c /etc/supervisor/supervisord.conf