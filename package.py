import os
import sys


app_name = "keyword-ws"
docker_registry= "localhost:5555"
docker_registry_tag = docker_registry + "/" + app_name

env = sys.argv[1] if len(sys.argv) > 1 else "dev"

def git_pull():
    run_cmd("git pull")

def change_config_file():
    print "changing config file"

    bootstrap_yml = open("src/main/resources/bootstrap.yml", "w")
    bootstrap_yml.truncate()

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

def restore_config_file():
    print "restore config file to dev"

    bootstrap_yml = open("src/main/resources/bootstrap.yml", "w")
    bootstrap_yml.truncate()

    dev_yml = "bootstrap-dev.yml"

    dev_yml_path = "src/main/resources/" + dev_yml
    dev_yml_file = open(dev_yml_path)

    for line in dev_yml_file:
        bootstrap_yml.write(line)

    bootstrap_yml.close()
    dev_yml_file.close()

    print "using " + dev_yml_path + " as config file"

def package_jar():
    change_config_file()
    run_cmd("mvn clean")
    run_cmd("mvn package -Dmaven.test.skip=true")
    restore_config_file()

def build_image():
    run_cmd("docker build --tag=" + app_name + " --force-rm=true .")
    run_cmd("docker tag " + app_name + " " + docker_registry_tag)
    run_cmd("docker push " + docker_registry_tag)

def k8s_deploy():
    run_cmd("kubectl apply -f kubernetes.yaml")

def run_sudo_cmd(cmd):
    cmd = "sudo " + cmd
    print cmd
    os.system(cmd)

def run_cmd(cmd):
    print cmd
    os.system(cmd)

if __name__ == '__main__':
    git_pull()
    package_jar()
    build_image()
    k8s_deploy()
