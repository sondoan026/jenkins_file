pipeline {
    agent { label '153_giao_viec' }

    stages {
        stage('Checkout') {
            steps {
                git branch: "${env:Branch_Name}",
                credentialsId: "${env:Git_Credentials_Id}",
                url: "https://github.com/thonguyenduc2010/odoo18.git"
            }
        }

    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}