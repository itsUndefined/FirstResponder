import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { AngularFireModule } from '@angular/fire';
import { AngularFireAuthModule } from '@angular/fire/auth';
import { AngularFirestoreModule, SETTINGS as FIRESTORE_SETTINGS } from '@angular/fire/firestore';

import { GoogleMapsModule } from '@angular/google-maps';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { environment } from '../environments/environment';
import { AuthComponent } from './auth/auth.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { UnauthorizedComponent } from './unauthorized/unauthorized.component';

import { NgbAlertModule } from '@ng-bootstrap/ng-bootstrap';
import { AlertStatusComponent } from './alert-status/alert-status.component';

@NgModule({
  declarations: [
    AppComponent,
    AuthComponent,
    DashboardComponent,
    UnauthorizedComponent,
    AlertStatusComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    AngularFireModule.initializeApp(environment.firebase),
    AngularFireAuthModule,
    AngularFirestoreModule,
    GoogleMapsModule,
    NgbAlertModule
  ],
  providers: [
    {
      provide: FIRESTORE_SETTINGS,
      useValue: environment.production ? undefined : {
        host: 'localhost:8080',
        ssl: false
      }
    }
],
  bootstrap: [AppComponent]
})
export class AppModule { }
