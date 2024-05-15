# Gitea

## Ansible Roles

### init-setup

complete role for setting up dockerized gitea instance.

- connected to a postgres database
- connected with one act runner

the gitea instance is extended with
- a complete nginx setup included with certbot from shared tasks

for enhanced workflows, a sonarqube instance connected to a postgres is included.

#### run

```
ansible-playbook -i inventory.ini playbook.yaml --ask-become-pass --ask-vault-pass --tags "init-setup"
```

