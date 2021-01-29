import os
import sys


app_name = "keyword-ws"
docker_registry= "localhost:5555"
docker_registry_tag = docker_registry + "/" + app_name

def git_pull():
    run_cmd("git pull")

def change_config_file():
    print "changing config file"

    bootstrap_yml = open("src/main/resources/bootstrap.yml", "w")
    bootstrap_yml.truncate()

    env = sys.argv[1] if len(sys.argv) > 1 else "dev"

    if env == "dev":
        env_yml = "bootstrap-dev.yml"
    elif env == "test":
        env_yml = "bootstrap-test.yml"
    elif env == "prod":
        env_yml = "bootstrap-prod.yml"

    env_yml_path = "src/main/resources/" + env_yml
    env_yml_file = open(env_yml_path)

    for line in env_yml_file:
        bootstrap_yml.write(line)

    bootstrap_yml.close()
    env_yml_file.close()

    print "using " + env_yml_path + " as config file"

def package_jar():
    run_cmd("mvn clean")
    run_cmd("mvn package -Dmaven.test.skip=true")

def build_image():
    run_cmd("docker build --tag=" + app_name + " --force-rm=true .")
    run_cmd("docker tag " + app_name + " " + docker_registry_tag)
    run_cmd("docker push " + docker_registry_tag)

def k8s_deploy():
    run_cmd("kubectl delete deployment " + app_name)
    run_cmd("kubectl create deployment " + app_name + " --image=" + docker_registry_tag)


def run_sudo_cmd(cmd):
    cmd = "sudo " + cmd
    print cmd
    os.system(cmd)

def run_cmd(cmd):
    print cmd
    os.system(cmd)

if __name__ == '__main__':
    git_pull()
    change_config_file()
    package_jar()
    build_image()
    k8s_deploy()
