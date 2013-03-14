VERSION=0.0.3
PEGASUS_MOD=pegasus
PEGASUS_PACKAGE_NAME=policy-pegasus
ADAPT_MOD=adapt
ADAPT_PACKAGE_NAME=policy-adapt
DIST_DIR=dist
BUILD_DIR=build

dist_pegasus: build_pegasus
	cd ${PEGASUS_MOD}/target && tar -czf ../../${DIST_DIR}/${PEGASUS_PACKAGE_NAME}-${VERSION}.tar.gz ${PEGASUS_PACKAGE_NAME}-${VERSION}.war

dist_adapt: build_adapt
	cd ${BUILD_DIR} && tar -czf ../${DIST_DIR}/${ADAPT_PACKAGE_NAME}-${VERSION}.tar.gz ${ADAPT_PACKAGE_NAME}
	
build_pegasus: prepare
	mvn install --projects pegasus -DskipTests=true

build_adapt: prepare
	mvn install --projects adapt -DskipTests=true
	mkdir -p ${BUILD_DIR}/${ADAPT_PACKAGE_NAME}/lib
	cp -f ${ADAPT_MOD}/target/*.jar ${BUILD_DIR}/${ADAPT_PACKAGE_NAME}/lib
	#rm -f ${BUILD_DIR}/${ADAPT_PACKAGE_NAME}/lib/junit*.jar
	#rm -f ${BUILD_DIR}/${ADAPT_PACKAGE_NAME}/lib/ptm*.jar
	#rm -f ${BUILD_DIR}/${ADAPT_PACKAGE_NAME}/lib/bestman2-client*.jar
	#rm -f ${BUILD_DIR}/${ADAPT_PACKAGE_NAME}/lib/log4j*.jar
	cp -f ${ADAPT_MOD}/src/main/java/edu/isi/policy/adapt/DemoAdaptClient.java ${BUILD_DIR}/${ADAPT_PACKAGE_NAME}
	cp -f ${ADAPT_MOD}/src/main/resources/* ${BUILD_DIR}/${ADAPT_PACKAGE_NAME}
	cp -f ${ADAPT_MOD}/scripts/* ${BUILD_DIR}/${ADAPT_PACKAGE_NAME}

prepare:
	mkdir -p ${DIST_DIR}
	mkdir -p ${BUILD_DIR}

clean:
	mvn clean
	rm -rf ${DIST_DIR}
	rm -rf ${BUILD_DIR}

