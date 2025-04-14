# build phase
COPY . /app/

# Create Maven settings.xml with GitHub authentication
RUN mkdir -p /app/.m2
RUN echo '<settings><servers><server><id>github</id><username>'${GITHUB_USERNAME}'</username><password>'${GITHUB_TOKEN}'</password></server></servers></settings>' > /app/.m2/settings.xml

# Run Maven with the custom settings file
RUN --mount=type=cache,id=s/e748f2be-628d-49c6-83aa-472ebc471f57-m2/repository,target=/app/.m2/repository \
    mvn -s /app/.m2/settings.xml -DoutputFile=target/mvn-dependency-list.log -B -DskipTests clean dependency:list install