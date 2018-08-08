**Требования**  
1. Java 8 (Oracle JDK/Oracle)
2. Tomcat

**Инструкция по установке rules-engine**  
1. Установить Java 8 и Tomcat 6 или выше.  
```
sudo dpkg -i openjdk-7-jdk_24.0-b56-2_amd64.deb
sudo dpkg -i tomcat_6.0.18-10_amd64.deb
```
Для установки только пакета tomcat без openjdk-7 команда:  
`sudo dpkg -i --ignore-depends=openjdk-7-jdk tomcat_6.0.18-10_amd64.deb`  
После этого нельзя будет ставить пакеты с помощью утилиты apt, т.к. будет отсутствовать зависимость openjdk-7.  

2. Указать переменную JAVA_HOME в init-файле сервиса /etc/init.d/tomcatd (существующий экспорт переменной JAVA_HOME необходимо закомментировать с помощью символа #):  
`export JAVA_HOME=/usr/java/jre1.8.0_121`  

3. Перезапустить сервис tomcatd:  
`sudo service tomcatd restart`  

4. Зайти на веб-интерфейс менеджера приложений tomсat: http://localhost:8080/manager/html  
Login: tomcat  
Password: tomcat  

5.1. Если есть существующее приложение simcraftwebapi, то нажать "Undeploy" напротив него.  

5.2. В разделе "WAR file to deploy" нажать "Выберите файл", указать файл **simcraft-web-api.war** и нажать "Deploy".  

Если менеджер приложений tomсat отсутствует, то надо положить/заменить файл по адресу /usr/local/tomcat/webapps/simcraft-web-api.war (через несколько минут он развернётся сам)  

**Проверка работоспособности**  
http://localhost:8080/simcraft-web-api/engines  

****REST API****

****Утилиты****

Запрос версии системы

    GET http://localhost:8080/simcraft-web-api/utils/version

Чтобы посмотреть логи - на 7м сервере выполнить

    GET http://localhost:8080/simcraft-web-api/utils/logs?last=1000