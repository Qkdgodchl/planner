import datetime
from pydantic import BaseModel

class Question(BaseModel):
    subject : str
    content : str
    create_date : datetime.datetime

