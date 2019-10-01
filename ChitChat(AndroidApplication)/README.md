# Project Description
<div style="text-align: justify">
"ChitChat" is a real-time chatting application developed in Android platform using firebase real-time database. It supports both one-to-one and group communication. User can send plain text and share images through the application and categorize the images according to the features of the images. User can set image resolution while sending pictures. 
</div> 

## Project folder structure

    .
    ├── frontend                     # Folder for android app          
    │   ├── ChatApp                  # The android app folder
    ├── google-services.json         # The project's google-services.json for Android
    ├── deploy.sh                    # The script for building and deploying
    ├── backend                      # Folder for backend (Firebase)
    │   ├── firebase_rules           # Folder for firebase access rules
    │   │   ├── database.rules.json  # Access rules for firebase realtime database
    │   │   ├── storage.rules        # Access rules for firebase storage
    │   └── functions                # Folder for firebase cloud functions js project
    

## Getting started
Use the following commands to deploy firebase functions and build android application:
- Deployment **script help**:
```bash
./deploy.sh
```

- To deploy **firebase backend**:
```bash
./deploy.sh backend
```
- To deploy **android application**:
 ```bash
./deploy.sh android
```
- To deploy both **firebase cloud function and android application**:
 ```bash
./deploy.sh all
```

<!--Use the package manager [pip](https://pip.pypa.io/en/stable/) to install foobar.

```bash
pip install foobar
```-->

## Important Dependencies
- For **firebase real-time database**
```bash
'com.google.firebase:firebase-core:16.0.5'
'com.google.firebase:firebase-auth:16.0.5'
'com.google.firebase:firebase-database:16.0.5'
'com.google.firebase:firebase-storage:16.0.5'
```
- For **firebase MLKit**
```bash
'com.google.firebase:firebase-ml-vision:17.0.0'
'com.google.firebase:firebase-ml-vision-image-label-model:15.0.0'
```
- For **circular image view** [hdodenhof](https://github.com/hdodenhof/CircleImageView)
```bash
'de.hdodenhof:circleimageview:2.2.0'
```
- For **image loading and caching** [Glide](https://github.com/bumptech/glide)
```bash
'com.github.bumptech.glide:glide:4.8.0'
```
- For **displaying images** [Picasso](http://square.github.io/picasso/)
```bash
'com.squareup.picasso:picasso:2.71828'
```
- For **image zooming** [saphiroth](https://github.com/sephiroth74/ImageViewZoom)
```bash
'it.sephiroth.android.library.imagezoom:imagezoom:+'
```
<!--```python
import foobar

foobar.pluralize('word') # returns 'words'
foobar.pluralize('goose') # returns 'geese'
foobar.singularize('phenomena') # returns 'phenomenon'
```-->

## Application Features

### User Authentication
- Unique username
- Upload profile image (Optional)
- Password must be at least six character long.

### User profile setting
- Tap on the profile image to change
- Tap on the username to change
- Tap on the image resolution to change

### Image Resolution
- LOW (640 X 480 px)
- HIGH (1280 X 960 px)
- FULL (Original image) Default

### Image Gallery
- All send and recieve image are stored
- Images can be sorted by date, sender, label

### One-to-one conversation
- User can send plain text.
- User can share image.

### Group conversation
- Any user can create group.
- Any user can add members to the group.
- Members can leave the group anytime.

### Notification
- User will be notified when he/she is added to any group.
- User will be notified when he/she receives a new message.

## Build with
- MVP (Model-View-Presenter) - Software design pattern used
- Java (Front end)
- Firebase cloud functions (Back end)