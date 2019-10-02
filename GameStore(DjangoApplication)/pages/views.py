from django.shortcuts import render

# pages/views.py
from django.views.generic import TemplateView


# loading the home page of the application
class HomePageView(TemplateView):
    template_name = 'home.html'

# loading the privacy page of the application
class Privacy(TemplateView):
    template_name = 'privacy.html'
