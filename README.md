# vertx-file-server
Simple permission-to-folder based file server

**Feautures:**
* File uploads
* File listing
* Users and corresponding permissions
* Permissions ("r" or "w" or "rw") for specific folder or file.
* Time based permissions ("User Bob can read "/specific/folder/*" until 2017 12 30 14:30)
* Public permission, without authentification
* Editing permission config from web. Require admin user

**Example config:**

```
users = [
  {"login": "admin", "pass": "111"},
  {"login": "Bob", "pass": "222"}
  {"login": "Andrey", "pass": "333"}
]

permissions = [
  {"id": "p1", "file": "someDir", "perm": "rw"},
  {"id": "p2", "file": "someDir/sub/dir", "perm": "r","until": "2018-04-08 12:30"},
  {"id": "pPub", "file": "someDir/somePubDir/someFile.jpg", "perm": "r", "until": "2019-04-08 12:30"},
]

usersHasPermissions = [
  {"login": "admin", "permissions": ["p1","p2"]},
  {"login": "Bob", "permissions": ["p2"]},
  {"login": "*", "permissions": ["pPub"]}, #public, no login required
]
```
**How to run:**

```
java -Dadmin="adminUser" -DconfigFile="/path/to/my.conf" -Dport=9999 -DrootDir="/files/etc" -DreloadInterval=10 -jar vertx-file-server.jar
```
**Params:**
* `-Dadmin` - who can edit config
* -DconfigFile - path to conf
* -Dport - http server port
* -DrootDir - dir where all permitted dirs exist
* -DreloadInterval - reload config interval(secs) if changed externally
