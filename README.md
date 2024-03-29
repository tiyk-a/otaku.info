# オタク.インフォ.v0.1 *Simple as much as possible*

### Compile Command
```
$ mvn package -Drun.jvmArguments="-Dfile.encoding=UTF-8"
```

### Java Run
```
$ nohup java -jar -Duser.language=ja -Duser.country=JP -Dfile.encoding=UTF-8 -Dspring.profiles.active=pro info-0.0.1-SNAPSHOT.jar & >> app.log
```

### 機能
#### 新商品情報の配信
* アフィリエイトサイトから`「グループ名　検索ワード」`で検索し、新商品のアフィリエイトURLを生成する。
    * 楽天アフィリエイトに対応
    * 20分ごとに上記検索を実行
* アフィリエイトサイトから`「アーティスト名　検索ワード」`で検索し、新商品のアフィリエイトURLを生成する。
    * 楽天アフィリエイトに対応
    * 40分ごとに上記検索を実行
* 検索結果があればPythonアプリケーションに新商品のデータを送信、Twitterに投稿する。

#### 新商品のリマインダー
* 今日から1年以内の未来に発売が予定されている商品を毎日20時ごろにお知らせ、予約OK？の投稿を行う。
    * 楽天アフィリエイトに対応
    * 発売3日前になったらリマインダーは停止。新商品発売カウントダウンがあるから
    * リマインド対象日は1ヶ月前、20日前、15日前、10日前、１週間前、5日前、4日前
    * 「新商品発売カウントダウン」のバッチをこちらにmergeしたほうが良い（予定）

#### 新商品発売カウントダウン
* 新商品の発売3日前から午前中にリマインダーを投稿。

#### 新商品発売日アナウンス
* 新商品発売日になったら朝早めにアナウンス。

#### TV出演情報の取得指示＆登録
【04:00】
* 毎日定時にPythonアプリ（pyTwi2）へTV出演情報の取得リクエストを投げる。
* TV出演情報の検索結果を受け取り、DBに保存する。
* 当日の情報のみ取得

#### TV出演情報のTwitter投稿指示
【06:00】
* 毎朝、本日のTV出演情報をグループごとに集め、pyTwi2へ投稿する指示を出す。
【21:00】
* 毎晩、翌日のTV出演情報をグループごとに集め、pyTwi2へ投稿する指示を出す。
【**:30】
* 1時間以内に始まるTV番組を集め、pyTwi2へ投稿する指示を出す。

#### Itemに入ってしまった不適切な商品はdel_flgをonにする
【/moveToDelItem/id-id-id】
* Itemに入った新商品が不適切な場合、上のパスへURLリクエストを投げることでItemから該当商品を削除、DelItemへ追加します。
* 商品IDをハイフンで区切ることで複数商品の削除を1リクエストで行います。

#### 新情報のチェック依頼を投げる
【19:00】
* 新しいデータが`Item, Program`に登録されていたらLINEへ通知を送ります。

#### タグをつけます
* ひとまずグループ名をつける
* 【予定】アーティスト名も任意でつけるようにする

#### Pending Diffを見れるようにするやつ
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;

#### 絵文字の使用
文字数削減のために絵文字使うことにしました
###### logger.debug
🕊Twitter投稿
💬LINE通知

###### Github
🐛Bug fix

# TODO
https://npnl.hatenablog.jp/entry/20070724/1185294796
https://aoking.hatenablog.jp/entry/20110917/1316242496
[PAST] IM mergeする機能があるけどmergeする前のidとかを残すカラムがないよ？

```
// Method2
Item a = itemService.findByItemId(1L).orElse(new Item());
Item b = itemService.findByItemId(1L).orElse(new Item());
b.setImage1("test");
DiffNode diff = ObjectDifferBuilder.buildDefault().compare(a, b);

boolean i = diff.hasChanges();
boolean d = diff.childCount() == 1;
// diffの中身には差分のある項目しか存在しない。第一引数を第二引数と比べてそのステータスを見ている。
DiffNode.State c = diff.getChild("site_id").getState();
DiffNode.State f = diff.getChild("image1").getState();
<dependency>
    <groupId>de.danielbechler</groupId>
    <artifactId>java-object-diff</artifactId>
    <version>0.95</version>
</dependency>
```

# PMの管理について
PMは放送内容情報、放送時間、放送局リスト、チーム情報、メンバー情報を持つ。
別時間の同じ内容の放送や再放送は別物として管理する。
Q：関連放送や番組を見つけたい時は？
A：番組名などで検索すれば引っかかるはず

# 楽天ROOMのAPI
## ユーザーIDに対して
https://room.rakuten.co.jp/api/1000004141968196/likes_collect?limit=100&after_id=1700187672595828
https://room.rakuten.co.jp/api/1000001788282356/collects?limit=100

## コレIDに対して
https://room.rakuten.co.jp/api/1700183991491820/users_liked?limit=100

# 楽天ROOMのフロント画面
## ユーザー画面
https://room.rakuten.co.jp/9191/items
→ユーザー名がわからないといけない

## コレ画面
https://room.rakuten.co.jp/9191/1700183991491820