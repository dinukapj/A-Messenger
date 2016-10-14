# A-Messenger
### Installation

  Import the following line into your Module `build.gradle`
 ```sh
compile 'com.github.kaodim:A-Messenger:0.1'
```
### Dependences
The Project is depended on the following libriaries
```
compile 'com.github.chrisbanes:PhotoView:1.2.6' 
compile 'com.googlecode.android-query:android-query:0.25.9'
compile 'com.squareup.picasso:picasso:2.5.2'
compile 'com.google.code.gson:gson:2.4'
```
### How to use
###### Override `ConversationActivity` and provide the endpoints For your backend to pull the list of conversations
.......
````java
public class SubConversationActivity extends ConversationsActivity {\
    @Override
    protected ArrayList<ConversationModel> fromJsonToConverstionModelArray(String s) {
        return gson.fromJson(s,new TypeToken<List<Conversation>>() {
        }.getType());
    }

    @Override
    protected String getConversationUrl() {
        return  https://YourBackendApi.com/api/conversations.json;
    }

    @Override
    protected Class<? extends ChatActivity> getChatActivityChild() {
        return SubChatActivity.class;
    }
}

````
###### Override `ChatActivity` and provide the endpoints For your backend to pull messages
.......
````java
public class SubChatActivity extends ChatActivity {

    @Override
    public String getMessageThreadURL(String conversationId) {
        return return  https://YourBackendApi.com/api/conversations.json?id=conversationId;
    }

    @Override
    protected MessageModel fromJsonToMessageModel(String s) {
        return gson.fromJson(s, YourOriginalMessageModel.class);
    }

    @Override
    protected ChatModel fromJsonToChatModel(String s) {
        return gson.fromJson(s, YourOriginalChatModel.class); 
        // ChatModel includes ArrayList if MessageModels
    }
}
````

