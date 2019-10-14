//import org.everit.json.schema.Schema;
//import org.everit.json.schema.ValidationException;
//import org.everit.json.schema.loader.SchemaLoader;
//import org.json.JSONObject;
//import org.json.JSONTokener;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//public class Validater {
//
//    public static void main(String[]args) {
//        System.out.println("Hello world");
//        String myString = "{\"components\":{\"requestBodies\":{\"UserArray\":{\"content\":{\"application\\/json\":{\"schema\":{\"type\":\"array\",\"items\":{\"$ref\":\"#\\/components\\/schemas\\/User\"}}}},\"description\":\"List of user object\",\"required\":true},\"Pet\":{\"content\":{\"application\\/json\":{\"schema\":{\"$ref\":\"#\\/components\\/schemas\\/Pet\"}},\"application\\/xml\":{\"schema\":{\"$ref\":\"#\\/components\\/schemas\\/Pet\"}}},\"description\":\"Pet object that needs to be added to the store\",\"required\":true}},\"securitySchemes\":{\"petstore_auth\":{\"type\":\"oauth2\",\"flows\":{\"implicit\":{\"authorizationUrl\":\"http:\\/\\/petstore.swagger.io\\/api\\/oauth\\/dialog\",\"scopes\":{\"write:pets\":\"modify pets in your account\",\"read:pets\":\"read your pets\"}}}},\"api_key\":{\"type\":\"apiKey\",\"name\":\"api_key\",\"in\":\"header\"}},\"schemas\":{\"Order\":{\"title\":\"Pet Order\",\"description\":\"An order for a pets from the pet store\",\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\",\"format\":\"int64\"},\"petId\":{\"type\":\"integer\",\"format\":\"int64\"},\"quantity\":{\"type\":\"integer\",\"format\":\"int32\"},\"shipDate\":{\"type\":\"string\",\"format\":\"date-time\"},\"status\":{\"type\":\"string\",\"description\":\"Order Status\",\"enum\":[\"placed\",\"approved\",\"delivered\"]},\"complete\":{\"type\":\"boolean\",\"default\":false}},\"xml\":{\"name\":\"Order\"}},\"Category\":{\"title\":\"Pet category\",\"description\":\"A category for a pet\",\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\",\"format\":\"int64\"},\"name\":{\"type\":\"string\"}},\"xml\":{\"name\":\"Category\"}},\"User\":{\"title\":\"a User\",\"description\":\"A User who is purchasing from the pet store\",\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\",\"format\":\"int64\"},\"username\":{\"type\":\"string\"},\"firstName\":{\"type\":\"string\"},\"lastName\":{\"type\":\"string\"},\"email\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"phone\":{\"type\":\"string\"},\"userStatus\":{\"type\":\"integer\",\"format\":\"int32\",\"description\":\"User Status\"}},\"xml\":{\"name\":\"User\"}},\"Tag\":{\"title\":\"Pet Tag\",\"description\":\"A tag for a pet\",\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\",\"format\":\"int64\"},\"name\":{\"type\":\"string\"}},\"xml\":{\"name\":\"Tag\"}},\"Pet\":{\"title\":\"a Pet\",\"description\":\"A pet for sale in the pet store\",\"type\":\"object\",\"required\":[\"name\",\"photoUrls\"],\"properties\":{\"id\":{\"type\":\"integer\",\"format\":\"int64\"},\"category\":{\"$ref\":\"#\\/components\\/schemas\\/Category\"},\"name\":{\"type\":\"string\",\"example\":\"doggie\"},\"photoUrls\":{\"type\":\"array\",\"xml\":{\"name\":\"photoUrl\",\"wrapped\":true},\"items\":{\"type\":\"string\"}},\"tags\":{\"type\":\"array\",\"xml\":{\"name\":\"tag\",\"wrapped\":true},\"items\":{\"$ref\":\"#\\/components\\/schemas\\/Tag\"}},\"status\":{\"type\":\"string\",\"description\":\"pet status in the store\",\"enum\":[\"available\",\"pending\",\"sold\"]}},\"xml\":{\"name\":\"Pet\"}},\"ApiResponse\":{\"title\":\"An uploaded response\",\"description\":\"Describes the result of uploading an image resource\",\"type\":\"object\",\"properties\":{\"code\":{\"type\":\"integer\",\"format\":\"int32\"},\"type\":{\"type\":\"string\"},\"message\":{\"type\":\"string\"}}}}}}";
//        InputStream is = new ByteArrayInputStream( myString.getBytes() );
//            JSONObject rawSchema = new JSONObject(new JSONTokener(is));
//            Schema schema = SchemaLoader.load(rawSchema);
//            try {
//                schema.validate(new JSONObject("{\"category\":{\"id\":\"drdrrdr\",\"name\":\"string\"},\"photoUrls\":[\"string\"],\"tags\":[{\"id\":0}],\"status\":\"available\"}")); // throws a ValidationException if this object is invalid
//            } catch(ValidationException e) {
//
//                System.out.println("Schema validation failed in the Response :" + e.getMessage());
//            }
//
//    }
//}
