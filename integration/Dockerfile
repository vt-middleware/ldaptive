FROM maven:3.9-amazoncorretto-__JDK__
RUN yum update -y && \
  yum install -y procps net-tools openldap-clients krb5-workstation && \
  yum clean all && \
  rm -rf /var/cache/yum
