# -*- coding: utf-8 -*-
from __future__ import unicode_literals
from urllib.request import urlopen
from django.shortcuts import render, get_object_or_404, redirect
from django.http import HttpResponse
from django.urls import reverse
from .models import Games, Purchase, Game_Details, Game_log, Admin_log, Purchase_log
from hashlib import md5
import datetime
from users.models import CustomUser
from django.urls import reverse_lazy
from django.views import generic
from django.http import HttpResponseRedirect
from django.contrib.auth.decorators import login_required
from .decorators import developer_required
from .decorators import player_required
from django.contrib.auth import logout
from django.views.decorators.csrf import csrf_exempt
from django.http import JsonResponse
from django.contrib import messages
from django.db.models import Count, Sum
from django.views.generic import TemplateView

# Home page of our gamestore application
@login_required(login_url='login')
def index(request):
    if request.user.role == '1':
        return developer_home(request)
    elif request.user.role == '2':
        return player_home(request)

# This function is used to show all the game uploaded by developer.
@login_required(login_url='login')
@developer_required
def developer_home(request):
    sold_games = list()
    sold_count = 0
    profit = 0
    current_user = request.user
    userid = get_object_or_404(CustomUser, pk=current_user.id)
    request.session['developerid'] = userid.id # developer id is stored in session for later use
    if request.method == 'POST':
        # search by name and category
        search_game_name = request.POST.get('search')
        search_game_category = request.POST.get('category')
        if search_game_name:
            gamelist = Games.objects.filter(name_of_the_game__icontains = search_game_name,
                                            userid = current_user.id)
        elif search_game_category and search_game_category != "Select Category":
            gamelist = Games.objects.filter(category__icontains = search_game_category,
                                            userid = current_user.id)
        elif search_game_name and search_game_category:
            gamelist = Games.objects.filter(name_of_the_game__icontains = search_game_name,
                                            category__icontains = search_game_category,
                                            userid = current_user.id)
        else:
            gamelist =  Games.objects.filter(userid=current_user.id)

    else:
        gamelist = Games.objects.filter(userid=current_user.id)

    # for developer statistics
    sold_gamelist = Purchase_log.objects.filter(userid = current_user.id)
    # total uploaded game
    total_uploaded_games = Games.objects.filter(userid=current_user.id)
    game_count = len(total_uploaded_games)
    # total number of sold games and profit
    for game in sold_gamelist:
        sold_count += 1
        profit += game.price

    profit = round(profit, 2)
    context = {'dev_home':'Developer Home','gamelist':gamelist,'game_count':game_count,'profit':profit,'sold_count':sold_count,'developer_name':userid.username}
    return render(request,'gamestore/developer_home.html',context)


# Player home will show all the game he/she has already purchashed.
# when the game is purchased the checksum will be calculated.
# Checksum will be compared with the received one, if it matches then the player home page will be loaded.
@login_required(login_url='login')
@player_required
@csrf_exempt
def player_home(request):
    purchased_gamelist = list()
    purch_gamelist = list()
    current_user = request.user
    userid = get_object_or_404(CustomUser, pk=current_user.id)
    request.session['playerid'] = userid.id # developer id is stored in session for later use
    now = datetime.datetime.now()
    pid = request.GET.get('pid') # purchase id
    ref = request.GET.get('ref') # reference number for each purchase
    result = request.GET.get('result') # result based on succes, cancel, error
    checksum = request.GET.get('checksum') # checksum from payment service
    secret = "0eedc3701a7f6f821b5838e49c20d3d7" # secret token generted using secret id
    # calculating checksum
    checksumstr = "pid={0}&ref={1}&result={2}&token={3}".format(pid, ref, result, secret)
    m = md5(checksumstr.encode("ascii")) # creating the digest of the checksum
    calculated_checksum = m.hexdigest()
    if checksum == calculated_checksum:
        gameid = get_object_or_404(Games, pk=request.session['gameid'])
        gameprice = request.session['gameprice']
        purchase_date = now.strftime("%Y-%m-%d")
        purchase_time = now.strftime("%H:%M")
        try:
            data = Purchase(date_purchased = purchase_date,time_purchased = purchase_time,
                            price_purchased = gameprice, userid = userid, gameid = gameid)
            data1 = Purchase_log(name_of_the_game = gameid.name_of_the_game, price = gameprice, userid = gameid.userid)
            data.save() # storing the information in Purchase table
            data1.save() # storing the information in Purchase_log (used for statistics) table
        except:
            return HttpResponse("Game Already Purchased")

        gamelist = Games.objects.all()
        purchased_game = Purchase.objects.filter(userid=userid)
        for purch in purchased_game:
            purch_gamelist.append(purch.gameid.id)
        for game in gamelist:
            if game.id in purch_gamelist:
                purchased_gamelist.append(game)
        
        # Total number of purchased games
        game_count = len(purchased_gamelist)
        context = {'player_home':"Player Home Page",'gameid':gameid,'gameprice':gameprice,"gamelist":purchased_gamelist,'game_count':game_count,'player_name':userid.username}
        return render(request,'gamestore/player_home.html',context)
    else:
        gamelist = Games.objects.all()
        purchased_game = Purchase.objects.filter(userid=userid)
        for purch in purchased_game:
            purch_gamelist.append(purch.gameid.id)
        for game in gamelist:
            if game.id in purch_gamelist:
                purchased_gamelist.append(game)
        #  Total number of purchased games
        game_count = len(purchased_gamelist)
        context = {'player_home':"Player Home Page","gamelist":purchased_gamelist,'game_count':game_count,'player_name':userid.username}
        return render(request,'gamestore/player_home.html',context)


# This function will be used to upload game by developer
@login_required(login_url='login')
@developer_required
def developer_upload(request):
    now = datetime.datetime.now()
    current_user = request.user
    userid = get_object_or_404(CustomUser, pk=current_user.id)
    name = request.POST.get('name')
    description = request.POST.get('description')
    url = request.POST.get('url')
    category = request.POST.get('category')
    date = now.strftime("%Y-%m-%d")
    time = now.strftime("%H:%M")
    price = request.POST.get('price')
    data = Games(name_of_the_game=name,url_of_the_game=url,price=price,date_added=date,time_added=time,description=description,category=category, userid=userid)
    context = {'upload_game': "Upload New Game"}
    if name is not None and description is not None and url is not None and category is not None and price is not None and date is not None and time is not None:
        data.save() # store information in Game table
        messages.success(request, 'Game is uploaded successfully.')
    return render(request,'gamestore/developer_upload_game.html', context)



# this function will be used for showing sale statistics of the games
@login_required(login_url='login')
@developer_required
def developer_statistics(request):
    current_user = request.user
    profit = 0
    sold_gamelist = Purchase_log.objects.filter(userid = current_user.id).values('name_of_the_game').annotate(total=Sum('price'),counter=Count('name_of_the_game')).order_by('name_of_the_game')
    context = {'dev_home': 'Purchase Statistics', 'gamelist': sold_gamelist}
    return render(request,'gamestore/developer_purchase_statistics.html', context)


# This function will be used for deleting games by developer
@login_required(login_url='login')
@developer_required
def developer_delete_game(request):
    sold_games = list()
    sold_count = 0
    profit = 0
    sold_gamelist = Purchase.objects.all()
    userid = get_object_or_404(CustomUser, pk=request.session['developerid'])
    if request.method == 'POST':
        game_id = request.POST.get('gameid')
        game = Games.objects.get(pk = game_id)
        game.delete() # delete game from the Game database
        gamelist = Games.objects.filter(userid = userid)

        sold_gamelist = Purchase_log.objects.filter(userid = userid)
        # total uploaded game
        game_count = len(gamelist)
        # total number of sold games and profit
        for game in sold_gamelist:
            sold_count += 1
            profit += game.price
        profit = round(profit, 2)
    else:
        gamelist = Games.objects.filter(userid = userid)

        sold_gamelist = Purchase_log.objects.filter(userid = userid)
        # total uploaded game
        game_count = len(gamelist)
        # total number of sold games and profit
        for game in sold_gamelist:
            sold_count += 1
            profit += game.price
        profit = round(profit, 2)
    context = {'dev_home':"Developer Home",'gamelist':gamelist,'game_count':game_count,'profit':profit,'sold_count':sold_count}
    return render(request,'gamestore/developer_home.html',context)


# This function will be triggered when the modify button will be pressed to update the game data.
@login_required(login_url='login')
@developer_required
def developer_update_gameval(request):
    if request.method == 'POST':
        game_id = request.POST.get('gameid')
        request.session['gameid'] = game_id
    else:
        game_id = request.session['gameid']

    game = Games.objects.get(pk = game_id)
    context = {'upload_game':'Update Game', 'game':game}
    return render(request,'gamestore/developer_update_game.html', context)


# This function is used to complete the update process of the game by developer
@login_required(login_url='login')
@developer_required
def developer_update_game_completed(request):
    profit = 0
    sold_games = list()
    sold_count = 0
    now = datetime.datetime.now()
    if request.method == 'POST':
        game_id = request.session['gameid']
        game_modified_date = now.strftime("%Y-%m-%d")
        game_modified_time = now.strftime("%H:%M")
        game = Games.objects.get(pk = game_id)
        game.name_of_the_game = request.POST.get('name')
        game.description = request.POST.get('description')
        game.url_of_the_game = request.POST.get('url')
        game.category = request.POST.get('category')
        game.price = request.POST.get('price')
        game.save() # update the information of the game in Game table

        gameid = get_object_or_404(Games, pk=game_id)
        userid = get_object_or_404(CustomUser, pk=request.session['developerid'])
        data = Admin_log(date_modified=game_modified_date,time_modified = game_modified_time,
                            price_updated = request.POST.get('price'),
                            description_modified = request.POST.get('description'),
                            category_modified = request.POST.get('category'),
                            url_updated = request.POST.get('url'),
                            userid = userid,gameid = gameid )
        data.save() # store information about the updated game to Admin_log table

    gamelist = Games.objects.filter(userid = request.user.id)
    sold_gamelist = Purchase.objects.all()
    # total uploaded games
    game_count = len(gamelist)
    # total number of sold games and profit
    for sold_game in sold_gamelist:
        sold_games.append(sold_game.gameid.id)
    for game in gamelist:
        if game.id in sold_games:
            sold_count += 1
            profit += game.price

    profit = round(profit, 2)
    context = {'dev_home':"Developer Home",'gamelist':gamelist,'game_count':game_count,'profit':profit,'sold_count':sold_count}
    return render(request,'gamestore/developer_home.html',context)


# This function lists available games for player.
# It will only show those game which is not purchases by the player.
@login_required(login_url='login')
@player_required
def player_available_gamelist(request):
    available_gamelist = list()
    purch_gamelist = list()
    player = get_object_or_404(CustomUser, pk=request.session['playerid'])
    if request.method == 'POST':
        # search games by name of the game and category
        search_game_name = request.POST.get('search')
        search_game_category = request.POST.get('category')
        if search_game_name:
            gamelist = Games.objects.filter(name_of_the_game__icontains = search_game_name)
        elif search_game_category and search_game_category != "Select Category":
            gamelist = Games.objects.filter(category__icontains = search_game_category)
        elif search_game_name and search_game_category:
            gamelist = Games.objects.filter(name_of_the_game__icontains = search_game_name, category__icontains = search_game_category)
        else:
            gamelist = Games.objects.all()
    else:
        gamelist = Games.objects.all()

    purchased_game = Purchase.objects.filter(userid = player)
    for purch in purchased_game:
        purch_gamelist.append(purch.gameid.id)
    for game in gamelist:
        if game.id not in purch_gamelist:
            available_gamelist.append(game)
    context = {'player_header':'Purchase Game','gamelist':available_gamelist}
    return render(request,'gamestore/player_available_gamelist.html',context)


# This function is used for bill payment.
# sid, pid, secret and amount is used to generate checksome.
# session values are created to use gameid and gameprice after the payment is completed.
@login_required(login_url='login')
@player_required
def bill_payment(request):
    if request.method == 'POST':
        game_id = request.POST.get('gameid')
    else:
        game_id = request.session['gameid']
    game = Games.objects.get(pk = game_id)
    sid = "thunderbolt007"
    pid = "thunderboltpayment"
    secret = "0eedc3701a7f6f821b5838e49c20d3d7"
    amount = game.price
    # generating the checksum by creating digest of the concatenated string
    checksumstr = "pid={0}&sid={1}&amount={2}&token={3}".format(pid, sid, amount, secret)
    m = md5(checksumstr.encode("ascii"))
    checksum = m.hexdigest()
    request.session['gameid'] = game.id # storing the gameid in session for later use
    request.session['gameprice'] = game.price # storing the price in session for later use
    context = {'checksum':checksum,'pid':pid,'sid':sid,'game':game}
    return render(request,'gamestore/bill_payment.html',context)


# When the play button is clicked, player_gameplay function triggered
@login_required(login_url='login')
@player_required
@csrf_exempt
def player_gameplay(request):
    all_scores = list()
    highest_score = 0
    individual_score = 0
    try:
        if request.POST.get('gameid'):
            game = Games.objects.get(pk = request.POST.get('gameid'))
            request.session['gameid'] = game.id
        else:
            game = Games.objects.get(pk = request.session['gameid'])
    except Games.DoesNotExist:
        return player_home(request)
    try:
        game_details = Game_Details.objects.filter(gameid = request.session['gameid']).latest('score')
        highest_score = game_details.score
        context = {'player_home': 'Game Play', 'game':game, 'highest_score': highest_score}
    except:
        highest_score = 0
        context = {'game':game, 'highest_score': highest_score}

    return render(request,'gamestore/player_gameplay.html',context)


# this function is used for storing the game state when save button is pressed
@login_required(login_url='login')
@player_required
@csrf_exempt
def player_gamestate(request):
    if request.is_ajax():
        userid = get_object_or_404(CustomUser, pk=request.session['playerid'])
        gameid = get_object_or_404(Games, pk=request.session['gameid'])
        game_state = request.POST.get('state')
        game_score = request.POST.get('score')
        now = datetime.datetime.now()
        score_date = now.strftime("%Y-%m-%d")
        score_time = now.strftime("%H:%M")
        #save game_state in the DB
        try:
            game_state_obj = Game_log.objects.get(userid = userid,gameid =gameid)
            game_state_obj.save_state = game_state
            game_state_obj.save() # game state is updated in Game_log table
        except Game_log.DoesNotExist:
            data = Game_log(status=0, save_state=game_state,userid = userid,gameid =gameid  )
            data.save()
        #add Score into the DB
        try:
            game_details_obj = Game_Details.objects.get(userid = userid,gameid = gameid)
            game_details_obj.score = game_score
            game_details_obj.date = score_date
            game_details_obj.time = score_time
            game_details_obj.save() # game details is updated in Game_Details table
        except Game_Details.DoesNotExist:
            data = Game_Details(score=game_score,date = score_date,time = score_time,userid = userid,gameid =gameid )
            data.save() # game details is stored in Game_details table
        return render(request,'gamestore/player_home.html')


# this function is used for storing the submitted score when submit score button is pressed
@login_required(login_url='login')
@player_required
@csrf_exempt
def player_gamescore(request):
    if request.is_ajax():
        game_score = request.POST.get('score')
        game = Games.objects.get(pk = request.session['gameid'])
        now = datetime.datetime.now()
        score_date = now.strftime("%Y-%m-%d")
        score_time = now.strftime("%H:%M")
        userid = get_object_or_404(CustomUser, pk=request.session['playerid'])
        gameid = get_object_or_404(Games, pk=request.session['gameid'])
        try:
            game_details_obj = Game_Details.objects.get(userid = userid,gameid = gameid)
            game_details_obj.score = game_score
            game_details_obj.date = score_date
            game_details_obj.time = score_time
            game_details_obj.save() # game details is updated in Game_Details table
        except Game_Details.DoesNotExist:
            data = Game_Details(score=game_score,date = score_date,time = score_time,userid = userid,gameid =gameid )
            data.save() # game details is stored in Game_details table
        context = {'individual_score':game_score}
        return render(request,'gamestore/player_home.html',context)


# this function is used for loading the previous state of the game
@login_required(login_url='login')
@player_required
@csrf_exempt
def player_gameload(request):
    if request.is_ajax():
        userid = get_object_or_404(CustomUser, pk=request.session['playerid'])
        gameid = get_object_or_404(Games, pk=request.session['gameid'])
        response_data ={}
        try:
            game_state = Game_log.objects.get(userid = userid, gameid = gameid)
            response_data['value'] = game_state.save_state
        except Game_log.DoesNotExist:
            response_data ={}
    return JsonResponse(response_data)
