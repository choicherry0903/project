from pathlib import Path
from reportlab.pdfgen import canvas
from reportlab.lib.colors import HexColor, white
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Paragraph
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.enums import TA_LEFT

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'output/pdf/interior-cms-portfolio.pdf'
OUT.parent.mkdir(parents=True, exist_ok=True)
pdfmetrics.registerFont(TTFont('KR', 'C:/Windows/Fonts/malgun.ttf'))
pdfmetrics.registerFont(TTFont('KRB', 'C:/Windows/Fonts/malgunbd.ttf'))
pdfmetrics.registerFontFamily('KR', normal='KR', bold='KRB')
W, H = 595.28, 841.89
M = 46
INK, MUTED, ACCENT = map(HexColor, ['#172B3A', '#586975', '#087F80'])
PALE, LINE = map(HexColor, ['#EDF6F5', '#DCE5E9'])
c = canvas.Canvas(str(OUT), pagesize=(W,H))
c.setTitle('지붕과벽 CMS | 웹 개발 프로젝트 포트폴리오')
c.setAuthor('Interior CMS')

def text(s, x=M, y=0, size=10, bold=False, color=INK):
    c.setFillColor(color); c.setFont('KRB' if bold else 'KR', size)
    c.drawString(x, H-y, s)

def para(s, y, size=10, width=W-2*M, x=M, color=INK, leading=None):
    style=ParagraphStyle('p', fontName='KR', fontSize=size, leading=leading or size*1.65,
                         textColor=color, wordWrap='CJK', alignment=TA_LEFT)
    p=Paragraph(s,style); _,ph=p.wrap(width,H)
    p.drawOn(c,x,H-y-ph)
    return y+ph

def start(n, label, title, subtitle):
    c.setFillColor(ACCENT); c.rect(0,H-9,W,9,fill=1,stroke=0)
    text('ROOF & WALL  /  ENGINEERING PORTFOLIO',y=36,size=8,bold=True,color=ACCENT)
    text(label,y=84,size=10,bold=True,color=ACCENT)
    text(title,y=122,size=25,bold=True)
    para(subtitle,141,size=10.2,color=MUTED)
    c.setStrokeColor(LINE); c.line(M,43,W-M,43)
    text('지붕과벽 CMS  ·  AI-assisted development',y=H-27,size=8,color=MUTED)
    text(f'{n:02d} / 04',x=W-M-37,y=H-27,size=8,color=MUTED)

def section(title, y, body):
    text(title,y=y,size=12,bold=True)
    return para(body,y+13)+22

def callout(title, body, y):
    style=ParagraphStyle('call',fontName='KR',fontSize=10,leading=16.5,textColor=INK,wordWrap='CJK')
    p=Paragraph(body,style); _,ph=p.wrap(W-2*M-30,H)
    height=ph+51
    c.setFillColor(PALE); c.roundRect(M,H-y-height,W-2*M,height,8,fill=1,stroke=0)
    text(title,x=M+15,y=y+23,size=11,bold=True,color=ACCENT)
    p.drawOn(c,M+15,H-y-35-ph)
    return y+height+23

def row(label, body, y):
    text(label,y=y+11,size=9.5,bold=True,color=ACCENT)
    end=para(body,y,size=9.5,x=M+99,width=W-2*M-99)
    c.setStrokeColor(LINE); c.line(M,H-end-10,W-M,H-end-10)
    return end+22

start(1,'01  PROJECT OVERVIEW','지붕과벽 CMS','고객·현장·상담을 연결하고, 데이터 변경의 안전성을 고려한 인테리어 업무 관리 시스템')
y=202
y=callout('프로젝트 한눈에 보기','인테리어 업체의 고객과 공사 현장, 상담 내용을 관리하는 서버 렌더링 웹 애플리케이션입니다. <b>바이브 코딩으로 제작</b>했으며, 현재 구현의 중심은 직원 인증과 고객·현장·상담 데이터의 등록·조회·수정·보관입니다.',y)
y=section('해결하려는 문제',y,'고객 정보와 현장, 상담 내용을 연결해 한 흐름에서 관리하고자 했습니다. 등록 과정의 일부 저장, 반복 요청에 따른 중복 등록, 동시 수정으로 인한 덮어쓰기, 다른 업체 데이터 접근을 주요 실패 상황으로 다뤘습니다.')
y=section('개발 방식과 AI 활용',y,'생성형 AI에 자연어로 요구사항과 수정 사항을 전달하고 코드를 생성·수정하는 바이브 코딩 방식을 활용했습니다. 문서는 현재 저장소에서 확인되는 구현을 기준으로 작성했습니다. AI 활용 자체와 별개로, 설계의 동작 원리와 실패 상황을 설명할 수 있도록 기술적 판단을 정리했습니다.')
y=row('Backend','Java 25 · Spring Boot 4.0.8 · Spring MVC · Spring JDBC',y)
y=row('View / Security','Thymeleaf · CSS · Spring Security · 세션 기반 인증',y)
y=row('Data / Test','PostgreSQL(Supabase) · H2 테스트 DB · Spring 통합 테스트',y)
y=section('구현 범위',y,'직원 로그인 및 로그아웃 / 고객·현장 목록과 상세 / 고객·현장·상담 일괄 등록 / 수정 및 고객 보관 / 업체별 접근 범위 제한 / 중복 요청 방지 / 버전 기반 수정 충돌 감지')
para('개발 기간·참여 인원·운영 성과는 제공된 정보가 없어 기재하지 않았습니다. 실제 AI 모델 호출, 검색, 파일 업로드는 현재 구현 범위에 포함되지 않습니다.',y,size=8.5,color=MUTED)
c.showPage()

start(2,'02  ARCHITECTURE & DATA','역할과 데이터의 경계','화면 요청, 업무 처리, SQL 실행을 분리하고 고객을 중심으로 데이터를 연결합니다.')
y=205
labels=['Controller','Service','Repository','PostgreSQL']
bw=(W-2*M-30)/4
for i,label in enumerate(labels):
    x=M+i*(bw+10)
    c.setFillColor(PALE); c.roundRect(x,H-y-43,bw,43,5,fill=1,stroke=0)
    text(label,x=x+10,y=y+26,size=10,bold=True,color=ACCENT)
    if i<3: text('>',x=x+bw+2,y=y+26,size=9,color=MUTED)
y=273
y=section('계층별 책임',y,'<b>Controller</b>는 요청 수신, 검증 결과와 화면 전환을 처리합니다. <b>Service</b>는 여러 저장 작업의 순서와 트랜잭션 경계를 관리합니다. <b>Repository</b>는 JdbcClient로 SQL을 실행하고, <b>DTO</b>는 조회 결과와 폼 입력 데이터를 전달합니다.')
y=section('데이터 모델',y,'<b>customers 1 : N construction_projects</b><br/>고객 한 명이 여러 현장을 가질 수 있습니다.<br/><br/><b>customers 1 : N customer_consultation_items</b><br/>상담 항목은 현장이 아니라 고객에 연결됩니다. 작성 직원은 author_id로 연결하며, 상담 순서는 sort_order로 보존합니다.')
y=callout('구조에서 드러나는 업무 규칙','목록의 한 행은 고객 한 명이 아니라 <b>현장 하나</b>입니다. 같은 고객이 여러 현장을 가지면 여러 행으로 표시됩니다. 고객을 보관하면 해당 고객에 연결된 모든 현장이 일반 조회에서 숨겨집니다.',y)
y=section('업체 범위와 데이터 연결',y,'업체 ID는 요청 파라미터가 아니라 로그인한 직원의 인증 정보에서 가져옵니다. 조회·변경 SQL에 company_id를 적용하고, 고객·현장 조인에도 업체 일치 조건을 둡니다. 상담 테이블은 복합 외래키로 고객·작성 직원이 같은 업체에 속하도록 제한합니다.')
y=section('선택의 이점과 비용',y,'JDBC는 기존 스키마와 조인·변경 조건을 SQL로 직접 표현하기 쉽습니다. 반면 매핑과 SQL을 직접 관리해야 합니다. 현재 DTO는 조회와 입력 역할을 함께 가지므로, 기능 확대 시 요청·응답 DTO와 조회·쓰기 서비스의 분리를 개선 과제로 두었습니다.')
c.showPage()

start(3,'03  RELIABILITY & SECURITY','실패 상황을 다루는 설계','일부 저장, 중복 요청, 동시 수정, 접근 범위 이탈에 각각 다른 장치를 적용합니다.')
y=205
y=section('01  여러 테이블 저장의 원자성',y,'<b>문제</b>  고객 저장 후 상담 저장이 실패하면 일부 데이터만 남을 수 있습니다.<br/><b>구현</b>  등록·수정 Service에 @Transactional을 적용했습니다. 등록은 고객 → 현장 → 상담을 묶고, 수정은 고객·현장 변경과 상담 항목 교체를 함께 처리합니다.<br/><b>효과</b>  롤백 대상 예외가 전파되면 해당 작업 전체를 취소합니다. Controller는 서비스 호출 실패를 받아 입력을 유지한 오류 화면을 제공합니다.')
y=section('02  반복 요청과 동시 등록',y,'<b>문제</b>  중복 클릭이나 재전송으로 같은 등록이 여러 번 실행될 수 있습니다.<br/><b>구현</b>  UUID 기반 requestKey, 사전 조회, DB 유니크 제약을 조합했습니다. 동시 요청에서 중복 예외가 발생하면 롤백 후 같은 업체에서 해당 요청의 완료 여부를 확인합니다.<br/><b>경계</b>  같은 요청의 중복 실행을 막습니다. 이름·연락처가 같은 고객 전체를 중복 판별하는 기능은 아닙니다.')
y=section('03  오래된 화면에서의 수정',y,'<b>문제</b>  두 직원이 같은 정보를 열고 수정하면 먼저 저장한 내용을 덮어쓸 수 있습니다.<br/><b>구현</b>  고객·현장 UPDATE에 version 조건을 적용하고 성공 시 증가시킵니다. 갱신 건수가 1건이 아니면 충돌 예외를 발생시켜 전체 변경을 롤백합니다.<br/><b>사용자 처리</b>  충돌 안내 후 최신 정보를 다시 불러오도록 합니다.')
y=section('04  인증과 데이터 접근 제한',y,'Spring Security의 세션 기반 로그인과 BCrypt 비밀번호 검증을 사용합니다. 업무 화면은 인증이 필요하며, 다른 업체의 현장 ID로 상세 조회 시 404로 처리합니다. POST에는 CSRF 보호, SQL에는 파라미터 바인딩, 고객 입력 출력에는 Thymeleaf 이스케이프를 적용합니다.')
para('설계상 유의점  ·  트랜잭션만으로 중복 요청이나 덮어쓰기가 모두 해결되지는 않습니다. 원자성은 트랜잭션으로, 중복은 유니크 제약으로, 수정 충돌은 버전 조건으로 각각 다룹니다.',y,size=9,color=ACCENT)
c.showPage()

start(4,'04  VERIFICATION & NEXT STEPS','검증 범위와 개선 방향','구현된 기능, 테스트 코드로 다룬 상황, 향후 확장을 구분합니다.')
y=205
y=section('테스트 전략',y,'별도 test 프로필과 메모리 H2를 사용한 통합 테스트를 구성했습니다. 정상 저장 여부 외에도 실제 요청 처리와 DB 결과를 함께 확인하는 시나리오를 포함합니다. 아래는 저장소의 테스트 코드에서 확인한 범위이며, 이번 문서 제작 과정에서 테스트를 재실행한 결과는 아닙니다.')
y=row('등록 / 수정','연결 데이터 저장, 상담 순서, 입력 검증·복원, 상담 제거, 저장 실패 시 전체 롤백',y)
y=row('정합성','동일 요청 재전송·동시 등록, 오래된 버전의 수정·삭제 거절, 충돌 시 변경 롤백',y)
y=row('접근 / 보안','다른 업체·보관 고객 제외, 로그인 필요 여부, CSRF, HTML 이스케이프',y)
y=section('현재 한계와 우선 개선 과제',y,'<b>1. PostgreSQL 기반 검증</b>  H2와 운영 DB의 차이를 보완하도록 실제 PostgreSQL에서 제약조건과 동시성 테스트를 추가합니다.<br/><b>2. 목록 조회 확장</b>  현재 전체 목록 조회에 페이지네이션을 도입하고, 실행 계획에 따라 인덱스와 쿼리를 검토합니다. 성능 수치는 아직 측정하지 않았습니다.<br/><b>3. 변경 이력 보존</b>  상담 수정은 삭제 후 재등록 방식입니다. 항목 ID·작성 정보 보존이 필요해지면 항목별 수정과 이력 관리를 도입합니다.')
y=section('AI 기능의 현재 상태',y,'AI 상담 화면과 별도 Python FastAPI 서비스의 초기 골격이 있습니다. /chat은 입력 메시지를 반환하는 수준이며 실제 모델 호출과 Spring 연동은 미구현입니다. 확장 시 개인정보 전달 범위, 타임아웃·실패 처리, 결과 검토 흐름을 먼저 정리할 계획입니다.')
y=callout('바이브 코딩 프로젝트의 기술적 설명 기준','AI로 생성한 결과물의 기능 나열을 넘어, <b>어떤 실패를 예상했고 어떤 경계와 제약으로 대응하는지</b>를 설명하는 데 초점을 맞췄습니다. AI 활용 사실을 공개하고, 구현된 동작과 미구현 계획을 구분해 제시합니다.',y)
para('근거 자료  ·  README 및 docs/architecture.md, 고객 Controller·Service·Repository·DTO, SecurityConfig, 상담 DDL, 인증·등록·수정·조회 통합 테스트. 현재 로컬 저장소 기준.',y,size=8,color=MUTED)
c.save()
print(OUT)
