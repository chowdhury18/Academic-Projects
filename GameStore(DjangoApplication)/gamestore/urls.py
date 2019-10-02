from django.urls import path
from django.conf.urls import include
from . import views

app_name = 'gamestore'
urlpatterns = [
    path('', views.index, name='index'),
    path('developer/', views.developer_home, name='developer_home'),
    path('developer/statistics', views.developer_statistics, name='developer_statistics'),
    path('developer/upload', views.developer_upload, name='developer_upload'),
    path('developer/deletegame', views.developer_delete_game, name='developer_delete_game'),
    path('developer/updateval/', views.developer_update_gameval, name='developer_update_gameval'),
    path('developer/updated/', views.developer_update_game_completed, name='developer_update_game_completed'),
    path('player/', views.player_home, name='player_home'),
    path('player/gamelist/', views.player_available_gamelist, name='player_available_gamelist'),
    path('player/gamebillpayment/', views.bill_payment, name='player_bill_payment'),
    path('player/gameplay/', views.player_gameplay, name='player_gameplay'),
    path('player/gameplay_score/', views.player_gamescore, name='player_gamescore'),
    path('player/gameplay_state/', views.player_gamestate, name='player_gamestate'),
    path('player/gameplay_load/', views.player_gameload, name='player_gameload'),
]
