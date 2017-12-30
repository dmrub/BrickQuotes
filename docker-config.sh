############### Configuration ###############

APP_NAME=BrickQuotes
IMAGE_PREFIX=${IMAGE_PREFIX:-brickquotes}
WAR_FILE=$APP_NAME.war
BUILD_PREFIX=${BUILD_PREFIX:-$IMAGE_PREFIX}
IMAGE_TAG=${IMAGE_TAG:-latest}
IMAGE_NAME=${IMAGE_PREFIX}:${IMAGE_TAG}

JAVA_MAVEN_DEPS=(
)

############# End Configuration #############
