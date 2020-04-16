import { Component, OnInit } from '@angular/core';
import { AngularFireAuth } from '@angular/fire/auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  templateUrl: './unauthorized.component.html',
  styleUrls: ['./unauthorized.component.css']
})
export class UnauthorizedComponent implements OnInit {

  constructor(private afa: AngularFireAuth, private router: Router) { }

  ngOnInit(): void {
  }

  onLogout() {
    this.afa.signOut().then(() => {
      this.router.navigateByUrl('login');
    });
  }

}
