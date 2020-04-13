import { Component } from '@angular/core';

import { AngularFireAuth } from '@angular/fire/auth';

import { auth } from 'firebase';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  constructor(private auth: AngularFireAuth) { }
  title = 'first-responder';

  async test() {
    // const result = await this.auth.createUserWithEmailAndPassword('themis.chatzie@gmail.com', 'testuser');

    const options: auth.ActionCodeSettings = {
      url: 'http://localhost:4200',
      handleCodeInApp: true
    };


    await this.auth.sendSignInLinkToEmail('themis.chatzie@gmail.com', options);

    // await this.auth.signInWithEmailLink('themis.chatzie@gmail.com');
  }
}
