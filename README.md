# Better auto-complete


This mod lets you easily autocomplete previous commands

Download the mod [here](https://modrinth.com/mod/better-command-completions) from modrinth

### Preview
![output](https://github.com/user-attachments/assets/92d3f11f-5907-4f30-8e41-7893f72c1342)


You can edit the file ```better_command_history.txt``` in the minecraft folder to add custom completions

You can add custom lines like
```
!10,/msg *** hello
```
The *** is a word wildcard (only 1 per command as of now) so any word can be placed there, make sure to put ! at the start of the line

### Misc info
It has a weight system so more previously used commands are prioritized in the suggestion
New modifications are loaded accross game restarts
The file is in a csv format weight,time_made,command except for lines starting with !
Completions lose 1 weight per day and stop at 0. A limit of 500 commands are saved is but can be changed by rebuilding the mod, commands with the lowest weights are replaced first in order of last used.
