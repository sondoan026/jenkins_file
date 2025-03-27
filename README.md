# Lấy mật khẩu để đăng nhập lần đầu
- docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword



# Cài java trên node tương ứng vs java trên server cài jenkins
- sudo apt-get update
- sudo apt-get upgrade
- sudo apt install openjdk-17-jdk openjdk-17-jre
- sudo update-alternatives --config java


# Tạo workspace cho jenkins trên server node
- sudo mkdir /var/lib/jenkins
- sudo chmod 777 /var/lib/jenkins
- sudo mkdir -p /var/lib/jenkins/.ssh
- sudo chown -R ubuntu2004:ubuntu2004 /var/lib/jenkins/.ssh
- sudo touch /var/lib/jenkins/.ssh/known_hosts
- sudo chmod 777 /var/lib/jenkins/.ssh/known_hosts
- ssh-keyscan 10.10.10.235 >> /var/lib/jenkins/.ssh/known_hosts

# Thêm quyền để chạy pipline
- sudo visudo
- ubuntu2004 ALL=(ALL) NOPASSWD: ALL
- sudo usermod -aG docker ubuntu2004
