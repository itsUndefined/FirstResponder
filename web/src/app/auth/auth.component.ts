import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup } from '@angular/forms';
import { AngularFireAuth } from '@angular/fire/auth';

import { auth } from 'firebase/app';
import 'firebase/auth';

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent implements OnInit {

  loginForm = new FormGroup({
    email: new FormControl(null),
    password: new FormControl(null)
  });

  serverMessage: string;

  rememberme = false;

  constructor(private afa: AngularFireAuth, private router: Router) { }

  ngOnInit(): void {
  }

  onLogin() {
    const credentials = this.loginForm.value;

    let persistence: string;
    if (this.rememberme) {
      persistence = auth.Auth.Persistence.LOCAL;
    } else {
      persistence = auth.Auth.Persistence.SESSION;
    }

    this.afa.setPersistence(persistence)
      .then(() => {
        return this.afa.signInWithEmailAndPassword(credentials.email, credentials.password);
      })
      .then(response => {
        this.router.navigateByUrl('/dashboard');
      }).catch(err => {
        this.serverMessage = err.message;
      });
  }

}
