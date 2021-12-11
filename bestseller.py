
# KINGBOOK
# 교보문고의 주간베스트 20의 데이터를 크롤링해와서 파이어베이스에 저장하는 프로그램.

# -*- coding: utf-8 -*-

from urllib.request import urlopen
from bs4 import BeautifulSoup
import firebase_admin

from firebase_admin import credentials
from firebase_admin import db

# 파이어베이스 주소 저장
db_url = 'https://kingbookdb-default-rtdb.asia-southeast1.firebasedatabase.app/'

# 파이어베이스 연결 (비공개 키 + 파이어베이스 주소)
cred = credentials.Certificate("kingbookdb-firebase-adminsdk-e89jh-6191d9d721.json")
default_app = firebase_admin.initialize_app(cred, {'databaseURL': db_url})


# 교보문고의 베스트셀러 웹페이지를 가져옵니다.
html = urlopen('http://www.kyobobook.co.kr/bestSellerNew/bestseller.laf')
soup = BeautifulSoup(html, "html.parser")

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
    author = []
    for name in authors:
        src = name.text.strip()
        author.append(src)
    author.pop()

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

    # 책 소개
    article = soup.select_one('div.box_detail_article').text.strip('</br>').strip()

    # 이미지 링크
    image = soup.find('meta', {'property': 'og:image'}).get('content')

    # 책 링크
    url = soup.find('meta', {'property': 'og:url'}).get('content')

    # 딕셔너리 형식으로 데이터 저장
    book_info = {
        'title': title,
        'author': author,
        'tag_list': tag,
        'article': article,
        'rank': rank,
        'url': url,
        'image': image
    }

    rank_info = {
        'title': title,
        'author': author,
        'rank': rank,
        'ISBN': ISBN13
    }

    # 파이어베이스에 데이터 삽입하는 부분
    # 베스트셀러 상세 정보 저장
    book_ref = db.reference('BOOK/BESTSELLER/%s' % (ISBN13))
    book_ref.set(book_info)

    # 데이터를 끌어온 날짜(mmdd)의 랭킹 정보를 저장.
    rank_ref = db.reference('RANK/%d/%d' % (1115, index))
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
