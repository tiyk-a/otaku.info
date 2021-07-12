# オタク.インフォ.v0.1

### 機能
#### 新商品情報の配信
* アフィリエイトサイトから`「アーティスト名　検索ワード」`で検索し、新商品のアフィリエイトURLを生成する。
    * 楽天アフィリエイトに対応
* 15分ごとに上記検索を実行
* 検索結果があればPythonアプリケーションに新商品のデータを送信、Twitterに投稿する。

#### 新商品のリマインダー
* 今日から1年以内の未来に発売が予定されている商品を毎日18時ごろにお知らせ、予約OK？の投稿を行う。
    * 楽天アフィリエイトに対応

#### 新商品発売カウントダウン
* 新商品の発売3日前から午前中にリマインダーを投稿。

#### 新商品発売日アナウンス
* 新商品発売日になったら朝早めにアナウンス
