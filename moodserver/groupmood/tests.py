# -*- coding: utf8 -*-
from django.utils import unittest
from django.test.client import Client
from django.utils import simplejson
import pprint

class Base(unittest.TestCase):
    def setUp(self):
        self.client = Client()

def get_related(context, result):
    contextUri = 'http://groupmood.net/jsonld/' + context
    return filter(lambda relation: context in relation['relatedcontext'], result['@relations'])[0]
        
class MeetingTest(Base):
    
    def test_create(self):
        response = self.client.post('/groupmood/meeting', {'name': 'Test-Meeting'}, Accept='application/json')
        self.assertEqual(response.status_code, 201)
        self.assertTrue('Location' in response)
        info = simplejson.loads(response.content)
        self.assertEquals(1, info['status']['code'])
        self.assertEquals('ok', info['status']['message'])
        self.assertEquals('http://groupmood.net/jsonld/meeting', info['result']['@context']) 
        self.assertEquals('Test-Meeting', info['result']['name'])
        self.assertEquals(1, info['result']['numTopics'])
        
    def test_default_vote(self):
        response = self.client.post('/groupmood/meeting', {'name': 'Test-Meeting'}, Accept='application/json')
        info = simplejson.loads(response.content)
        meeting = info['result']
        
        response = self.client.get(get_related('topic', meeting)['href'], Accept='application/json')
        self.assertEqual(response.status_code, 200)
        info = simplejson.loads(response.content)
        topics = info['result']
        self.assertEquals('http://groupmood.net/jsonld/topic', topics[0]['@context'])
        
        response = self.client.get(get_related('question', topics[0])['href'], Accept='application/json')
        self.assertEqual(response.status_code, 200)
        info = simplejson.loads(response.content)
        questions = info['result']
        self.assertEquals('http://groupmood.net/jsonld/question', questions[0]['@context'])
        
        self.assertEquals(50, questions[0]['avg'])
        self.assertEquals(0, questions[0]['numAnswers'])
        
        response = self.client.post(get_related('answer', questions[0])['href'], {'answer': 60}, Accept='application/json')
        response = self.client.post(get_related('answer', questions[0])['href'], {'answer': 70}, Accept='application/json')
        self.assertEqual(response.status_code, 201)
        
        response = self.client.get(questions[0]['@id'], Accept='application/json')
        info = simplejson.loads(response.content)
        self.assertEquals(65, info['result']['avg'])
        self.assertEquals(2, info['result']['numAnswers'])
        
