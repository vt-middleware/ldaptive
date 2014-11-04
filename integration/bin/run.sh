#!/bin/sh

TEST_GROUP=core
#TEST_GROUP=pool
#TEST_GROUP=core-sysprops
while [ $# -gt 0 ]
do
  case "$1" in
    -host)
      shift
      HOST=$1;;
    -baseDn)
      shift
      BASE_DN=$1;;
    -bindDn)
      shift
      BIND_DN=$1;;
    -bindCredential)
      shift
      BIND_CREDENTIAL=$1;;
    -group)
      shift
      TEST_GROUP=$1;;
    -provider)
      shift
      PROVIDER=$1;;
  esac
  shift
done
SSL_HOST=`echo ${HOST} |sed 's/389/636/'`

MVN_CMD="clean verify"
if [ ! -z "${PROVIDER}" ]; then
  MVN_CMD="-P${PROVIDER} ${MVN_CMD}"
fi

mvn \
  -DrunTests \
  -DldapTestHost=ldap://${HOST} \
  -DldapSslTestHost=ldap://${SSL_HOST} \
  -DldapBaseDn="${BASE_DN}" \
  -DldapBindDn="${BIND_DN}" \
  -DldapBindCredential="${BIND_CREDENTIAL}" \
  -DldapTestGroup=${TEST_GROUP} \
  -DldapTestsIgnoreLock=true \
  ${MVN_CMD}

