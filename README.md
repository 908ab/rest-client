# rest-client
maven repository


## Description
* JavaでRESTAPIを叩くときのクライアントクラス

## Usage
```
public class MemberRestClient extends AbstractRestClient<MemberReq, MemberRes> {
    public MemberRestClient(List<Header> headers, Long companyId) {
        super(
            "http://example.com/api/companies/{companyId}/members",
            headers,    // HTTPヘッダ
            Collections.singletonList(new BasicNameValuePair("companyId", companyId.toString())),
            MemberRes.class
        );
    }
    
    // 必要なHTTPメソッドをオーバーライド
    @Override
    public HttpResponse post(LectureReq body, String contentType) {
        return super.post(body, contentType);
    }
    
    
    // 自分でメソッドを定義して親クラスのメソッドを呼び出しても構いません
    public HttpResponse post(LectureReq body) {
        return super.post(body, "application/json");
    }
}
```

## Install
* maven
```
<dependencies>
    <dependency>
        <groupId>miyakawalab.tool</groupId>
        <artifactId>rest-client</artifactId>
        <version>${version}</version>
    </dependency>
</dependencies>
<repositories>
    <repository>
        <id>mongodb-dao</id>
        <url>https://raw.github.com/908ab/rest-client/mvn-repo/</url>
    </repository>
</repositories>
```

## Version
> 1.0

> 1.1
POST, PUTメソッドで文字化けする問題を解消