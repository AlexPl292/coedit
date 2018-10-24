# Test cases

This file describes manual test cases for this plugin

There test cases can be collected into groups. If test case passed, than you should move to next test case **with existing context**

Test cases use 2 IntelliJ IDEA instances with CoEdit plugin. Users are called Bob and Alice

## Test group 1


### Test case 1.0
Init state

Nor Alice or Bob are working with CoEdit plugin

*Check:*

- *Start server* and *Connect to server* are activated.
- *Stop edit and release lock* and *Stop collaboration* are deactivated

### Test case 1.1

*Steps:*

1. *Alice:* Click **Start server** button
2. *Alice:* Press **OK** button into dialog (default port)

*Check:*

- All menu punkts are deactivated 

### Test case 1.2

*Steps:*

1. *Bob:* Press **Connect to server** button
2. *Bob:* Press **OK** button (default port and host)

*Check:*

Alice and Bob
- *Start server* and *Connect to server* are deactivated.
- *Stop edit and release lock* and *Stop collaboration* are activated

### Test case 1.3

*Steps:*

1. *Alice:* Open file
2. *Alice:* Edit file

*Check:*

- *Bob* Open file and check that changes from Alice are received
- *Bob* Check that file has blue highlighting (guard block)

### Test case 1.4

*Steps:*

1. *Alice:* Open another file
2. *Alice:* Edit file

*Check:*

- *Bob* Check that blue highlighting has disappeared
- *Bob* Open another file and check that changes from Alice are received
- *Bob* Check that file has blue highlighting (guard block)

### Test case 1.5

*Steps:*

1. *Alice:* Click *Stop edit and release lock*

*Check:*

- *Bob* Check that blue highlighting has disappeared  
(This could be not immediately. Try to click on blue line)


### Test case 1.6

*Steps:*

1. *Bob:* Click *Stop collaboration* menu button

*Check:*

Alice and Bob
- *Start server* and *Connect to server* are activated.
- *Stop edit and release lock* and *Stop collaboration* are deactivated


## Test group 2

*Init steps:*

Start server and connect another side of plugin


### Test case 2.1

*Steps:*

1. *Bob:* Create new folder `TestFolder`

*Check:*
- *Alice:* Check that a new folder has been created


### Test case 2.2

*Steps:*

1. *Alice:* Create new file `TestFile.txt`

*Check:*
- *Bob:* Check that a new file has been created

### Test case 2.3

*Steps:*

1. *Alice:* Edit new created file `TestFile.txt`

*Check:*
- *Bob:* Check that changes are received from Alice

### Test case 2.4

*Steps:*
1. *Bob:* Delete folder `TestFolder`

*Check:*
- Folder is not deleted for Bob or Alice


### Test case 2.5

*Steps:*
1. *Bob:* Rename folder `TestFolder`

*Check:*
- Folder is not renamed for Bob or Alice

*Comment*

JI has strange behaviour on this point. 1) Folder is really not renamed, but you should check it very good.
Because additional folder (with new name) is created, and after that it's deleted. 2) Changed content of files
is not saved (probably because of guard?)

### Test case 2.6

*Steps:*
1. *Alice:* Press *Stop edit and release block* menu
2. *Bob:* Rename folder `TestFolder`

*Check:*
- Folder is renamed for Bob or Alice

### Test case 2.7

*Steps:*
1. *Bob:* Delete folder `TestFolder`

*Check:*
- Folder is deleted for Bob or Alice


## Test group 3

*Init steps:*

Start server and connect another side of plugin

### Test case 3.1

*Steps:*
1. *Bob:* Create folder `TestFolder`
2. *Bob:* Delete folder `TestFolder`

*Check:*
- Folder is created and deleted for Bob or Alice
