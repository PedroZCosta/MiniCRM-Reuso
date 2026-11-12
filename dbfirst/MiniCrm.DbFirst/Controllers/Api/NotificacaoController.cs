using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers.Api
{
    [Route("api/[controller]")]
    [ApiController]
    public class NotificacaoController : ControllerBase
    {
        private readonly MiniCrmContext _context;

        public NotificacaoController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: api/Notificacao
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Notificacao>>> GetNotificacao()
        {
            return await _context.Notificacao.ToListAsync();
        }

        // GET: api/Notificacao/5
        [HttpGet("{id}")]
        public async Task<ActionResult<Notificacao>> GetNotificacao(int id)
        {
            var notificacao = await _context.Notificacao.FindAsync(id);

            if (notificacao == null)
            {
                return NotFound();
            }

            return notificacao;
        }

        // PUT: api/Notificacao/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutNotificacao(int id, Notificacao notificacao)
        {
            if (id != notificacao.IdNotificacao)
            {
                return BadRequest();
            }

            _context.Entry(notificacao).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!NotificacaoExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }

        // POST: api/Notificacao
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<Notificacao>> PostNotificacao(Notificacao notificacao)
        {
            _context.Notificacao.Add(notificacao);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetNotificacao", new { id = notificacao.IdNotificacao }, notificacao);
        }

        // DELETE: api/Notificacao/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteNotificacao(int id)
        {
            var notificacao = await _context.Notificacao.FindAsync(id);
            if (notificacao == null)
            {
                return NotFound();
            }

            _context.Notificacao.Remove(notificacao);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool NotificacaoExists(int id)
        {
            return _context.Notificacao.Any(e => e.IdNotificacao == id);
        }
    }
}
