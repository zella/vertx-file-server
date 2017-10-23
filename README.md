# vertx-file-server
Simple permission-to-folder based file server

**Feautures:**
* File uploads
* File listing
* Users and corresponds permissions
* Permissions ("r" or "w" or "rw") for specific folder or file.
* Time based permissions ("User Bob can read "/specific/folder/*" until 2017 12 30 14:30)
* Public permission, without autorisation
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
  {"id": "pPub", "file": "someDir/somePubDir", "perm": "r", "until": "2019-04-08 12:30"},
]

usersHasPermissions = [
  {"login": "admin", "permissions": ["p1","p2"]},
  {"login": "Bob", "permissions": ["p2"]},
  {"login": "*", "permissions": ["pPub"]}, #public, no login required
]
```
