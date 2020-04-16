import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { canActivate, redirectLoggedInTo, hasCustomClaim, redirectUnauthorizedTo, customClaims } from '@angular/fire/auth-guard';

import { AuthComponent } from './auth/auth.component';
import { DashboardComponent } from './dashboard/dashboard.component';

import { pipe } from 'rxjs';
import { map } from 'rxjs/operators';
import { UnauthorizedComponent } from './unauthorized/unauthorized.component';
import { UnauthorizedGuard as onlyUnauthorized } from './unauthorized.guard';

const redirectLoggedInUsers = () => redirectLoggedInTo(['dashboard']);
const onlyDispatchers = () => pipe(customClaims, map(claims => claims.role === 'dispatcher' ? true : ['unauthorized']));

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: AuthComponent, ...canActivate(redirectLoggedInUsers) },
  { path: 'dashboard', component: DashboardComponent, ...canActivate(onlyDispatchers) },
  { path: 'unauthorized', component: UnauthorizedComponent, canActivate: [onlyUnauthorized] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
