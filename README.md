# CoEdit plugin

Plugin for collaborate project editing

### Functionality

Functionality of plugin is collected under `CoEdit` menu

- *Start Server* - start accepting incoming requests
- *Connect to server* - connect to running server
- *Stop edit and release lock* - release all locked files
- *Stop collaboration* - stop plugin work

### Lock behavior

When the user **A** starts editing the file, it is automatically locked for the user **B**.  
User **B** cannot edit a locked file until it's released.  
When user **A** starts to edit another file, old file is unlocked for user **B**.  

### Supported events

The following events are supported by plugin:
- File editing
- File/folder creation
- File/folder deletion
- File/folder renaming