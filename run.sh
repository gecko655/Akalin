SECRETENV_PATH="secretenv_release"
if [ $# -gt 0 ] && [ $1 = test ]; then
  SECRETENV_PATH="secretenv_test"
fi
docker run -d --name akalin  --env-file $SECRETENV_PATH gecko655/akalin
