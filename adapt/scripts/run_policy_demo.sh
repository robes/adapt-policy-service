#!/bin/sh
DIR=$(dirname $0)
CP=${DIR}
for i in ${DIR}/lib/*.jar; do
  CP=${CP}:${i}
done

if [ "${CP}x" != "x" ]; then
  CLASSPATH=${CLASSPATH}:${CP}
fi

java ${JAVA_OPTS} -cp "${CLASSPATH}" edu.isi.policy.adapt.DemoAdaptClient "${DIR}/policymodule.properties"

success=$?

exit ${success}

