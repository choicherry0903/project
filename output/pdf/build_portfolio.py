from pathlib import Path
from xml.sax.saxutils import escape
import xml.etree.ElementTree as ET
from reportlab.pdfgen import canvas
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.colors import HexColor, white
from reportlab.platypus import Paragraph
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.enums import TA_LEFT
import pypdfium2 as pdfium
from PIL import Image, ImageOps, ImageDraw
from pypdf import PdfReader

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'output/pdf/interior_cms_portfolio.pdf'
PREVIEW = ROOT / 'tmp/pdfs'
PREVIEW.mkdir(parents=True, exist_ok=True)
pdfmetrics.registerFont(TTFont('Malgun', 'C:/Windows/Fonts/malgun.ttf'))
pdfmetrics.registerFont(TTFont('MalgunBold', 'C:/Windows/Fonts/malgunbd.ttf'))
W, H = 595.276, 841.89
INK, MUTED, TEAL, LIGHT, LINE = [HexColor(x) for x in ['#172B36','#536875','#007E80','#EDF5F5','#D8E3E6']]
c = canvas.Canvas(str(OUT), pagesize=(W,H))
c.setTitle('지붕과벽 CMS | 개발 진행형 프로젝트 포트폴리오')
c.setAuthor('')
c.setSubject('실제 저장소의 구현과 통합 테스트를 바탕으로 작성한 기술 포트폴리오')

def text(x,y,s,size=10,color=INK,bold=False):
    c.setFillColor(color); c.setFont('MalgunBold' if bold else 'Malgun',size)
    c.drawString(x,H-y-size,s)

def para(s,x,y,width=499,size=10.2,color=INK,bold=False,leading=None):
    style=ParagraphStyle('p',fontName='MalgunBold' if bold else 'Malgun',fontSize=size,leading=leading or size*1.65,textColor=color,wordWrap='CJK',alignment=TA_LEFT)
    p=Paragraph(s,style); _,h=p.wrap(width,1000)
    assert y+h<779, (s[:50],y,h)
    p.drawOn(c,x,H-y-h); return y+h

def rect(x,y,w,h,fill=LIGHT,stroke=None,r=8):
    c.setFillColor(fill)
    c.setStrokeColor(stroke or fill)
    c.roundRect(x,H-y-h,w,h,r,fill=1,stroke=bool(stroke))

def line(x,y,x2,y2,color=LINE):
    c.setStrokeColor(color); c.setLineWidth(.7); c.line(x,H-y,x2,H-y2)

def header(num,eyebrow,title,desc):
    text(48,31,'ROOF & WALL / ENGINEERING PORTFOLIO',8,TEAL,True)
    text(48,77,eyebrow,9,TEAL,True)
    text(48,99,title,25,INK,True)
    para(desc,48,143,size=10.3,color=MUTED)
    line(48,192,547,192)

def footer(num,sources):
    line(48,782,547,782)
    text(48,792,'코드 검토 및 테스트 기준 2026.10.27',7.5,MUTED)
    text(507,792,f'{num:02d} / 05',7.5,MUTED)
    text(48,762,sources,6.6,MUTED)
    c.showPage()

def section(y,n,title,body):
    text(48,y,n,10,TEAL,True)
    text(78,y-2,title,13,INK,True)
    return para(body,78,y+23,width=469)+22

# 1. Executive overview
text(48,36,'PROJECT PORTFOLIO / 2026.10',9,TEAL,True)
rect(48,87,115,25)
text(61,92,'개발 진행 중',9,TEAL,True)
text(48,119,'개발 기간  1개월 미만  |  개인 프로젝트  |  담당 범위  전체 개발',9,MUTED)
text(48,148,'지붕과벽 CMS',34,INK,True)
para('<link href="https://github.com/choicherry0903/project.git" color="#007E80">GitHub  github.com/choicherry0903/project</link>',48,193,size=9)
para('인테리어 고객·현장·상담을<br/>하나의 업무 흐름으로 관리하는 웹 애플리케이션',48,223,size=16.5,bold=True,leading=26)
para('고객 등록부터 현장 조회, 상담 항목 편집, 기록 보관까지 연결한 Spring 기반 업무 관리 프로젝트입니다. 현재 구현에서 데이터 일관성, 직원 인증, 업체별 접근 범위를 함께 다룹니다.',48,291,size=11)

for x,value,label in [(48,'CRUD','고객·현장·상담 업무'),(219,'업체 범위','로그인 직원 기준 접근'),(390,'52 / 52','통합 테스트 통과')]:
    rect(x,358,157,94)
    text(x+15,374,value,19,TEAL,True)
    text(x+15,415,label,8.5,MUTED)

text(48,484,'현재 보여줄 수 있는 구현 역량',13,INK,True)
y=519
for title,body in [
    ('연결된 데이터 저장','고객·현장·상담 항목을 하나의 트랜잭션으로 저장하고 실패 시 롤백합니다.'),
    ('업무 데이터 보호','업체 조건을 적용한 조회·수정과 version 기반 변경 충돌 감지를 구현했습니다.'),
    ('실패 상황 검증','동시 등록, 중복 요청, 권한 범위, 저장 실패를 통합 테스트로 확인합니다.')]:
    text(48,y,title,10,TEAL,True)
    para(body,48,y+20,size=10)
    y+=66
para('기술 구성  Java 25 · Spring Boot 4.0.8 · Spring MVC · Thymeleaf<br/>Spring Security · Spring JDBC · PostgreSQL(Supabase) · H2(test)',48,718,size=8.5,color=MUTED,leading=14)
footer(1,'근거: pom.xml, README.md, src/main/java, target/surefire-reports')

# 2. Scope & journey
header(2,'01 / PRODUCT & SCOPE','핵심 업무 흐름과 구현 범위','현재 동작하는 기능과 확장할 기능을 구분해 프로젝트의 실제 범위를 설명합니다.')
text(48,215,'업무 목표',12,INK,True)
para('고객 정보, 현장 정보, 공종별 상담 내용을 함께 입력하고 다시 조회·수정할 수 있는 관리 흐름을 제공합니다. 상담은 분류·기타 항목명·내용을 가진 여러 행으로 구성합니다.',48,242)

for i,(title,body) in enumerate([('직원 로그인','계정 검증·세션'),('고객 목록','업체 내 현장 조회'),('등록·상세','연관 데이터 저장'),('수정·보관','충돌 확인·기록 유지')]):
    x=48+i*128
    rect(x,318,115,76)
    text(x+10,331,title,10,TEAL,True)
    text(x+10,358,body,7.7,MUTED)
    if i<3: text(x+118,342,'>',9,MUTED)
text(48,405,'코드 흐름을 요약한 개념도',7.5,MUTED)

rows=[
('고객·현장 목록','구현','업체별 조회, 건수, 정렬, 빈 목록, 상세 이동'),
('고객 등록·수정','구현','고객·현장·상담 동시 저장, 검증, 입력 복원'),
('고객 보관 처리','구현','확인 후 archived 처리, 해당 고객의 모든 현장 숨김'),
('직원 인증','구현','BCrypt 검증, 세션 로그인·로그아웃, CSRF'),
('AI 상담 정리','화면 골격','메뉴와 템플릿 존재, AI 호출 미구현'),
('검색·파일 업로드','미구현','현재 업무 흐름 확장 과제로 구분'),
]
text(48,438,'기능',8,MUTED,True); text(184,438,'현재 상태',8,MUTED,True); text(265,438,'확인 범위',8,MUTED,True)
y=465
for title,status,body in rows:
    line(48,y-7,547,y-7)
    text(48,y,title,9,INK,True)
    text(184,y,status,9,TEAL if status=='구현' else MUTED)
    para(body,265,y,width=282,size=8.7,leading=14)
    y+=43
para('현재 조회 단위는 현장이며, 상담 항목은 고객에 연결됩니다. 상태는 목록에서 표시하지만 상태 변경 기능은 현재 수정 범위에 포함되지 않습니다.',48,731,size=8.5,color=MUTED,leading=13)
footer(2,'근거: docs/screen-map.md, CustomerController.java, SceneController.java, 템플릿')

# 3. Architecture
header(3,'02 / ARCHITECTURE','요청부터 데이터까지','서버 렌더링 화면과 계층별 책임을 연결한 Spring MVC 구조입니다.')
nodes=[('BROWSER','Thymeleaf 화면','목록·등록·상세·수정·보관'),('SECURITY','Spring Security','인증 세션 · StaffPrincipal'),('CONTROLLER','Customer / Scene','요청 처리 · 허용 필드 · 입력 검증'),('SERVICE','CustomerQueryService','조회·쓰기 트랜잭션 · 변경 충돌 처리'),('REPOSITORY','CustomerRepository','JdbcClient · SQL · 업체 범위 조건')]
for i,(tag,title,body) in enumerate(nodes):
    y=215+i*71
    rect(48,y,499,57)
    text(62,y+10,tag,7.5,TEAL,True)
    text(163,y+9,title,10,INK,True)
    text(163,y+32,body,8.5,MUTED)
    if i<4: line(299,y+57,299,y+71,TEAL)
text(48,583,'PostgreSQL / interior_cms',11,TEAL,True)
para('staff_accounts의 company_id를 인증 정보에 담고, 고객·현장·상담 조회 및 변경 SQL에 전달합니다. DB 접속 정보는 환경 설정으로 분리합니다.',48,609,size=9.6)
text(48,661,'현재 사용 중인 핵심 데이터 관계',10,INK,True)
para('customers  1 : N  construction_projects<br/>customers  1 : N  customer_consultation_items<br/>staff_accounts  1 : N  customer_consultation_items (author_id)',48,686,size=9,leading=17)
text(48,743,'관계와 호출 흐름을 단순화한 개념도이며 전체 스키마 ERD는 아닙니다.',7.5,MUTED)
footer(3,'근거: SecurityConfig.java, StaffPrincipal.java, CustomerQueryService.java, CustomerRepository.java')

# 4. Engineering choices
header(4,'03 / ENGINEERING','정상 흐름 밖의 상황도 처리','중복 입력, 부분 저장, 동시 수정, 접근 범위를 코드와 테스트에서 함께 다룹니다.')
y=217
y=section(y,'01','부분 저장 방지',
    '고객 생성 뒤 현장이나 상담 저장이 실패하면 데이터가 일부만 남을 수 있습니다. 등록·수정 서비스에 <b>@Transactional</b>을 적용해 하나의 작업으로 묶고, 상담 저장 실패 시 앞선 변경도 롤백되는지 테스트합니다.')
y=section(y,'02','동일 등록 요청의 중복 방지',
    '등록 폼에 UUID 요청 키를 부여하고 기존 요청 여부와 DB 고유 제약을 함께 사용합니다. 중복 키 예외는 트랜잭션 롤백 후 완료된 요청인지 확인합니다. 순차 재전송과 두 개의 동시 요청을 테스트합니다.')
y=section(y,'03','수정 충돌 감지와 기록 보관',
    '고객·현장 수정 SQL에 <b>version</b> 조건을 넣고 갱신 건수가 1인지 확인합니다. 오래된 화면의 수정은 오류로 안내하고 앞선 변경을 롤백합니다. 삭제는 고객을 보관 처리하며 연결된 현장과 상담 기록을 유지합니다.')
y=section(y,'04','로그인 직원 기준의 접근 범위',
    '요청의 companyId 대신 인증된 <b>StaffPrincipal</b>의 업체 ID를 사용합니다. 폼 바인딩은 허용 필드를 지정하고, 타 업체 현장에 대한 접근 거부와 CSRF 누락 요청의 차단을 테스트합니다.')
rect(48,708,499,40)
text(62,718,'설계의 초점: 업무 입력의 일관성과 업체 범위를 지키는 CRUD',9,TEAL,True)
footer(4,'근거: CustomerController.java, CustomerQueryService.java, Repository, 등록·수정 통합 테스트')

# 5. Verification, next work
header(5,'04 / VALIDATION & NEXT','검증 결과와 다음 단계','2026.10.27에 저장소의 테스트 명령을 다시 실행한 결과입니다.')
reports=[]
for f in sorted((ROOT/'target/surefire-reports').glob('TEST-*.xml')):
    r=ET.parse(f).getroot()
    reports.append((r.attrib['name'].split('.')[-1],int(r.attrib['tests']),int(r.attrib['failures']),int(r.attrib['errors']),int(r.attrib['skipped'])))
assert len(reports)==7 and sum(r[1] for r in reports)==52 and sum(sum(r[2:]) for r in reports)==0
rect(48,213,499,69)
text(64,227,'52개 통과',23,TEAL,True)
text(255,239,'실패 0 / 오류 0 / 건너뜀 0',10,INK,True)
labels={'AuthIntegrationTest':'인증·접근 제어','CustomerDetailIntegrationTest':'고객 상세','CustomerInsertIntegrationTest':'기본 등록','CustomerListIntegrationTest':'고객 목록','CustomerModificationIntegrationTest':'수정·보관','CustomerRegistrationIntegrationTest':'연관 등록·실패·중복','ScreenScaffoldIntegrationTest':'화면·경로 렌더링'}
y=301
for name,count,*_ in reports:
    text(48,y,labels[name],9,INK,True)
    text(222,y,name,7.7,MUTED)
    text(518,y,str(count),9,TEAL,True)
    line(48,y+22,547,y+22)
    y+=31
para('실행: .\\scripts\\run-local.ps1 -Task test<br/>환경: test 프로필 · 메모리 H2 · MockMvc. 실제 PostgreSQL 환경의 통합 검증, 부하·성능 측정, 운영 배포 검증은 이 결과에 포함되지 않습니다.',48,528,size=8.5,color=MUTED,leading=14)
text(48,589,'후속 개발 제안',12,INK,True)
para('1. 검색·페이지네이션으로 고객 목록의 사용성 확장<br/>2. PostgreSQL 기반 검증 및 배포·관측 체계 정비<br/>3. 파일 업로드와 AI 상담 정리의 실제 처리 흐름 구현',48,617,size=9.8,leading=20)
rect(48,694,499,54)
para('이력서 소개 문장<br/><b>개발 기간 1개월 미만의 개인 프로젝트로 Spring 기반 인테리어 CMS 전체 개발을 진행 중입니다. 고객·현장·상담 CRUD, 직원 인증, 업체별 접근 제한, 트랜잭션·변경 충돌 감지를 구현하고 통합 테스트 52개를 검증했습니다.</b>',61,701,width=473,size=8.3,leading=13)
footer(5,'근거: target/surefire-reports/TEST-*.xml, src/test/java, README.md')
c.save()

# Validate text and render every page for visual review.
reader=PdfReader(OUT)
assert len(reader.pages)==5
for i,p in enumerate(reader.pages):
    t=p.extract_text()
    assert '지붕과벽' in t or 'PORTFOLIO' in t
doc=pdfium.PdfDocument(str(OUT))
thumbs=[]
for i in range(len(doc)):
    im=doc[i].render(scale=1.4).to_pil().convert('RGB')
    im.save(PREVIEW/f'page-{i+1}.png')
    im.thumbnail((417,590))
    thumbs.append(im)
sheet=Image.new('RGB',(417*3,616*2),'#ccd6dc')
for i,im in enumerate(thumbs):
    x=(i%3)*417; y=(i//3)*616
    sheet.paste(im,(x,y))
    ImageDraw.Draw(sheet).text((x+8,y+594),f'PAGE {i+1}',fill='black')
sheet.save(PREVIEW/'contact-sheet.png')
print(f'Created {OUT}; pages=5; tests=52; rendered all pages')
