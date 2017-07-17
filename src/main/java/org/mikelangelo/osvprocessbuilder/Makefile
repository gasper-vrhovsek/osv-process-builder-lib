DEST := stormy-java
#module: prepare $(DEST)/OsvProcess.class $(DEST)/OsvProcessBuilder.class $(DEST)/OsvProcessBuilder.so
module: prepare $(DEST)/OsvProcessBuilder.so

JAVAD=$(JAVA_HOME)
JAVAC=$(JAVAD)/bin/javac
JAVAH=$(JAVAD)/bin/javah
JAR=$(JAVAD)/bin/jar
CFLAGS=-g

prepare:
	mkdir -p $(DEST)

#$(DEST)/%.class: %.java
#	$(JAVAC) -cp ../../../ -d $(DEST) -target 7 -source 7 $^

clean:
	rm -rf $(DEST)
	#rm osv-process-builder.jar

# mini test
jni:
	export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.
	$(JAVAC) -cp ../../../ OsvProcessBuilder.java
	$(JAVAH) org.mikelangelo.osvprocessbuilder.OsvProcessBuilder
	gcc -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux -shared -fPIC libOsvProcessBuilder.c -o libOsvProcessBuilder.so
	java OsvProcessBuilder

$(DEST)/OsvProcessBuilder.so: libOsvProcessBuilder.c
	#$(JAVAH) -cp ../../../ -d $(DEST) org.mikelangelo.osvprocessbuilder.OsvProcessBuilder
	gcc $(CFLAGS) -I$(JAVAD)/include -I$(JAVAD)/include/linux -shared -fPIC libOsvProcessBuilder.c -o $(DEST)/libOsvProcessBuilder.so
	#$(JAR) -cf osv-process-builder.jar -C $(DEST) org/mikelangelo/osvprocessbuilder/OsvProcess.class -C $(DEST) org/mikelangelo/osvprocessbuilder/OsvProcessBuilder.class
