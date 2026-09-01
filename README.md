# Better auto-complete


This mod lets you easily autocomplete previous commands

**[Download here from Modrinth](https://modrinth.com/mod/better-command-completions)**

## Preview
![output](https://github.com/user-attachments/assets/92d3f11f-5907-4f30-8e41-7893f72c1342)

## Config
You can edit the file ```better_command_history.txt``` in the minecraft folder to add custom completions

You can add custom lines like
```
!10,/msg *** hello
```
The *** is a word wildcard (only 1 per command as of now) so any word can be placed there, make sure to put ! at the start of the line

### Misc info
It has a weight system so more previously used commands are prioritized in the suggestion

New modifications are loaded across game restarts

The file is in a CSV format weight,time_made,command except for lines starting with !

Completions lose 1 weight per day and stop when they reach 0
