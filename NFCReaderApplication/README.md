###### CS-E4300 Network security, 2018

# Project Report: Ticket Application by Group 01

<p><b>Arnab Chowdury			727312 </b></p></br>
<p><b>Osman Manzoor Ahmed		721347 </b></p></br>
<p><b>Kamrul Islam 			727590 </b></p></br>
<p><b>Nam Xuan Nguyen			727943 </b></p></br>

## Overview

We are developing a Ticket card that will be used for multi ride carousel. The ticket will have a fixed number of rides(5) with a time period
that will start from the first ride and will be valid till next midnight. For the renewal, the ticket holder will need to go to the ticket vendor
for the renewal. The ticket card will contain application tag, application version, expiry date and the maximum number of rides. The security majorly 
will cover tearing, MItM attacks. The more detailed about security will be discussed in the security section.

For conveniency, User will just need to tap the card on the reader for the ride. If the ticket is valid, user will be 
permitted to get to the ride otherwise he will not be permitted.

## Ticket application structure

<table>
  <tr>
    <td colspan="2"><b><center> Page address </center></b></td>
    <td colspan="4"><b><center> Byte number </center></b></td>
  </tr>
  <tr>
    <td><b><center> Decimal </center></b></td>
    <td><b><center> Hex </center></b></td>
    <td><b><center> 0 </center></b></td>
    <td><b><center> 1 </center></b></td>
    <td><b><center> 2 </center></b></td>
    <td><b><center> 3 </center></b></td>
  </tr>

  <tr>
    <td> 0 </td>
    <td> 00h </td>
    <td colspan="4"> serial number </td>
  </tr>

  <tr>
    <td> 1 </td>
    <td> 01h </td>
    <td colspan="4"> serial number </td>
  </tr>

  <tr>
    <td> 2 </td>
    <td> 02h </td>
    <td> serial number </td>
    <td> internal </td>
    <td> lock bytes </td>
    <td> lock bytes </td>
  </tr>

  <tr>
    <td> 4 </td>
    <td> 04h </td>
    <td colspan="4"> Application Tag </td>
  </tr>

  <tr>
    <td> 5 </td>
    <td> 05h </td>
    <td colspan="4"> Application Version </td>
  </tr>
  
  <tr>
    <td> 6 </td>
    <td> 06h </td>
    <td colspan="4"> Expiry Date </td>
  </tr>
  
  <tr>
    <td> 7 </td>
    <td> 07h </td>
    <td colspan="4"> Number of Rides </td>
  </tr>
  
  <tr>
    <td> 8 </td>
    <td> 08h </td>
    <td colspan="4"> MAC(User Data(Page 4 - Page 7)) </td>
  </tr>
  
  <tr>
    <td> ... </td>
    <td> ... </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>

  <tr>
    <td> ... </td>
    <td> ... </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>

  <tr>
    <td> ... </td>
    <td> ... </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>

  <tr>
    <td> 40 </td>
    <td> 28h </td>
    <td> lock bytes </td>
    <td> lock bytes </td>
    <td> - </td>
    <td> - </td>
  </tr>

  <tr>
    <td> 41 </td>
    <td> 29h </td>
    <td> 16-bit counter </td>
    <td> 16-bit counter </td>
    <td> - </td>
    <td> - </td>
  </tr>

  <tr>
    <td> 42 </td>
    <td> 2Ah </td>
    <td > 04h </td>
    <td> - </td>
    <td> - </td>
    <td> - </td>
  </tr>

  <tr>
    <td> 43 </td>
    <td> 2Bh </td>
    <td > 1111111[0] </td>
    <td> - </td>
    <td> - </td>
    <td> - </td>
  </tr>

  <tr>
    <td> 44 to 47 </td>
    <td> 2Ch to 2Fh </td>
<td colspan="4"> authentication key = Hash(UID | "....") </td>
  </tr>
</table>



The User Data will consist of the following fileds:
- **Page 4**:
	 - Application tag: This will specify the tag of the application that will be used by the reader to identify the specific application.
- **Page 5**:
	 - Application Version: This will specify the specific version, if there are multiple versions of the application.
- **Page 6**:
	 - Expiry Date: This will contain the date of the expiry. It will just be a date without any time span because the ticket is going to be valid till 
	next midnight.
- **Page 7**: 
	 - Number of Rides: This will specify the number of rides that are valid corresponding to the 16 bit counter value. If the 16 bit counter value is
	10 and the Number of rides contains 15, this means that the valid rides are (15-10) 5.
- **Page 8**: 
	 - MAC(User Data (Page 4 - Page 7)): This will contain the MAC of the user data that will be used to check the integrity of the data by the reader.
	   Only the reader will have the key. The MAC will be truncated to 4 bytes.
	 - The formula will look like this	
									 " truncate(HMAC-SHA256(key,User Data)) "
- **Page 42 (AUTH0)**:
	 - 04h: This specifies from where the authentication part starts.
- **Page 43 (AUTH1)**:
	 - 1111111[0]: Setting the first bit to 0 states that read and write access is restricted from the pages that are specified in AUTH0
- **Page 44 to 47**:
	 - authentication key: This contains the hash of the UID with some secret message that is only known by the reader. The reader will authenticate the
	   ticket card using this Hash.
	
In addition, this we will be using 16 bit counter to count the tickets used and comparing it with the value of the Number of Rides that is in the
User Data. Similarly the expiry date will be set once when the ticket card is used for the first ride.
	
		

## Key management

At the moment we are storing the above mentioned key that is used for the MAC as hardcoded one within the reader application only. The secret
message for the authentication key will also be saved within the reader only.

The secret message is stored within the reader, the reader will generate the authentication key again using the UID and the 
secret message. After this, authentication will be done using 3DES.

## Implementation

The implementation is basically divided into two parts. One is for issuing tickets and another is for validation.

### Issuing Tickets
- For the first time, we authenticate the card using the default authentication key then we format the card for the first time.
- Formating card for the first time contains the following operation:
  - **Issuing new authentication key**: Hash (UID(serial number) + "secret message")).
  - Issuing **application tag** and **version number**.
  - Set **expiration date** and **number of rides**.
    - **Number of rides**: read the 16-bit counter and add 5 more rides with it.
    - **Expiration date**: set to zero.
  - Issue **MAC** over **application tag, version, expiration date and number of rides**.
  - Set **AUTH0** and **AUTH1** as follows:
    - **AUTH0**: from page address 04h
    - **AUTH1**: set right most significant bit of the first byte to 0 (read and write permission is restricted without authentication)
- Format the card after the expiration date is over and when the card is compromised.
  - resetting the expiration date and number of rides to zero.
  - generate the MAC over the data.
  - after that the card is ready for issuing new tickets.

### Validating Tickets
- Set **expiration date** when the card is used for the **first ride**. The ticket is then valid till next midnight.
- **Increase the counter** value by one to keep track of the remaining rides.
- Before incrementing 16-bit counter, remaining number of rides and expiration is checked.
- Generate **MAC** over the data.
- MAC is also checked for suspicious activity (if the card is compromised)
- **Safe limit** is checked. Application will provide warning message when the number of remaining rides is more than 50 and the expiration date of the card is more than two days.



## Evaluation

### Security evaluation

What kind of security is achieved and any known weakesses.

Security Properties 

- **MitM attack**: 
    - By using the MAC scheme, MitM attack is mitigated.
- **Rollback attack**: 
    - 16-bit counter is used to protect against the rollback attack.
- **Tearing protection**: 
    - We modify one page (16-bit counter) at a time for tearing protection.


Weaknesses

- **Systemic failure caused by a leaked key**: 
    - Authentication key contains secret message which is only stored in reader application. If someone knows the
  secret key, he can generate the authentication key because hash function is meant to be public which is used to generate the key.

### Reliability and deployablity

Realibility of the system depends mainly on the ticket counter, we have used 16 bit counter for ticketing that can be incremented only and not 
decreased. So if no one can decrement the counter, we say that for ticketing the system is reliable. For deployability you just need to 
install mulitple readers at different places and the card will get authenticated on any reader as all are similar and all the data required 
for authentication is within the card.

## Final notes

- What if the secret message is compromised?
- What if the adversary used the counter to increment a valid ticket by a number so that it gets invalid? Then how to achieve tearing etc?
