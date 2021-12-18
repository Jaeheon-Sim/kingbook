# KINGBOOK
# 교보문고의 주간베스트 20의 데이터를 크롤링해와서 파이어베이스에 저장하는 프로그램.

# -*- coding: utf-8 -*-
import re
import urllib
from urllib.request import urlopen, urlretrieve
from bs4 import BeautifulSoup
from firebase_admin import db
from firebase_admin import credentials, initialize_app, storage

# 파이어베이스 주소 저장
db_url = 'https://helpme-378f8-default-rtdb.firebaseio.com/'
storage_url = 'helpme-378f8.appspot.com'

# 파이어베이스 연결 (비공개 키 + 파이어베이스 주소)
cred = credentials.Certificate("helpme-378f8-firebase-adminsdk-dwsva-df6ce4c883.json")
initialize_app(cred, {'databaseURL': db_url, 'storageBucket': storage_url})

# 교보문고의 베스트셀러 웹페이지를 가져옵니다.
html = urlopen('http://www.kyobobook.co.kr/bestSellerNew/bestseller.laf')
soup = BeautifulSoup(html, "html.parser")

date = soup.find('div', "content_middle").find('h4', "title_best_basic").find('small').text.strip()
sep_date = re.findall("\d+", date)
mmdd = sep_date[1] + sep_date[2]
# print(mmdd)

# 책의 상세 웹페이지 주소를 추출하여 리스트에 저장합니다.
book_page_urls = []
for cover in soup.find_all('div', {'class': 'detail'}):
    link = cover.select('a')[0].get('href')
    book_page_urls.append(link)

# 책의 상세 데이터를 크롤링해옵니다.
for index, book_page_url in enumerate(book_page_urls):
    index += 1
    html = urlopen(book_page_url)
    soup = BeautifulSoup(html, "html.parser")

    # 책 고유번호 - ISBN
    ISBN13 = soup.find('meta', {'property': 'eg:itemId'}).get('content')

    # 제목
    title = soup.find("h1", "title").find('strong').text.strip()

    # 작가
    authors = soup.select('div.author > span.name > a')
    a_list = []
    for name in authors:
        src = name.text.strip()
        a_list.append(src)
    a_list.pop()

    author = ""
    for n in a_list:
        author = author + n + ' '

    # 출판사
    publisher = src

    # 랭킹
    # rank = soup.select_one('div.rank > a > em').text.strip()
    rank = index

    # 가격
    price = soup.find('meta', {'property': 'og:price'}).get('content')

    # 태그
    tag_list = soup.select('div.tag_list > a > span > em')
    tag = []
    for t in tag_list:
        T = t.text.strip()
        tag.append(T)

    tags = ""
    for t in tag:
        tags = tags + t + ' '

    # 책 소개
    article = soup.select_one('div.box_detail_article').text.strip('</br>').strip()

    # 이미지 링크
    image = soup.find('meta', {'property': 'og:image'}).get('content')

    # 책 링크
    url = soup.find('meta', {'property': 'og:url'}).get('content')

    # 이미지 웹에서 다운로드해서 저장하기
    imgURL = str(image)
    imagePath = "D:/bookcrawling/{0}.jpg".format(ISBN13)

    urllib.request.urlretrieve(imgURL, imagePath)

    # 기본 버킷 사용
    bucket = storage.bucket()

    # 경로 지정
    blob = bucket.blob('bookImages/' + str(ISBN13))

    # 메타데이터에 책
    metadata = {"book_title": title}
    blob.metadata = metadata

    # 파이어베이스 파일 업로드
    blob.upload_from_filename(filename=imagePath, content_type='image/jpeg')

    blob.make_public()
    print("path:" + blob.public_url)

    # 딕셔너리 형식으로 데이터 저장
    book_info = {
        'title': title,
        'author': author,
        'tag_list': tags,
        'article': article,
        'rank': rank,
        'url': url,
        'image': str(blob.public_url)
    }

    rank_info = {
        'title': title,
        'author': author,
        'rank': rank,
        'ISBN': ISBN13,
        'tag_list': tags,
        'image': str(blob.public_url),
        'article': article
    }

    # 파이어베이스에 데이터 삽입하는 부분
    # 베스트셀러 상세 정보 저장
    book_ref = db.reference('BOOK/BESTSELLER/%s' % (ISBN13))
    book_ref.set(book_info)

    # 데이터를 끌어온 날짜(mmdd)의 랭킹 정보를 저장.
    rank_ref = db.reference('RANK/%s/%d' % (mmdd, index))
    rank_ref.set(rank_info)

    print(rank)
    print(title)
    print(author)
    print(price + "원")
    print(tag)
    print(article)
    print("image : " + image)
    print("url : " + url)
    print(ISBN13)
    print()
    print()
